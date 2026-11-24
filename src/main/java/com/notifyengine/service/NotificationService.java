package com.notifyengine.service;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.exception.ApiException;
import com.notifyengine.model.*;
import com.notifyengine.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationService {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    private final NotificationRepository notifications;
    private final NotificationLogRepository logs;
    private final RecipientRepository recipients;
    private final NotificationTypeRepository types;
    private final VendorSubscriptionRepository subscriptions;

    @Value("${notification.max-retries:4}")
    private int maxRetries;

    public NotificationService(NotificationRepository notifications, NotificationLogRepository logs,
                               RecipientRepository recipients, NotificationTypeRepository types,
                               VendorSubscriptionRepository subscriptions) {
        this.notifications = notifications;
        this.logs = logs;
        this.recipients = recipients;
        this.types = types;
        this.subscriptions = subscriptions;
    }

    /** Validates, renders the template and stores the notification as QUEUED. The worker sends it. */
    @Transactional
    public Notification queue(Client client, SendRequest req) {
        Recipient recipient = recipients.findByClientIdAndExternalRef(client.getId(), req.recipientRef())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recipient not found"));
        NotificationType type = types.findByClientIdAndCodeAndActiveTrue(client.getId(), req.typeCode())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification type not found"));

        List<VendorSubscription> subs = subscriptions
                .findByClientIdAndActiveTrueAndVendorChannelAndVendorStatusOrderByPriorityAsc(
                        client.getId(), type.getChannel(), Status.ACTIVE);
        if (subs.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "No active vendor subscription for channel " + type.getChannel());
        }

        if (type.getChannel() == Channel.EMAIL) {
            if (isBlank(recipient.getEmail())) throw new ApiException(HttpStatus.BAD_REQUEST, "Recipient has no email");
            if (!recipient.isEmailOptIn()) throw new ApiException(HttpStatus.BAD_REQUEST, "Recipient has not opted in to EMAIL");
        } else {
            if (isBlank(recipient.getPhone())) throw new ApiException(HttpStatus.BAD_REQUEST, "Recipient has no phone");
            if (!recipient.isSmsOptIn()) throw new ApiException(HttpStatus.BAD_REQUEST, "Recipient has not opted in to SMS");
        }

        Map<String, String> vars = req.variables() == null ? Map.of() : req.variables();

        Notification n = new Notification();
        n.setClient(client);
        n.setRecipient(recipient);
        n.setType(type);
        n.setChannel(type.getChannel());
        n.setSubject(render(type.getSubjectTemplate(), vars));
        n.setMessage(render(type.getBodyTemplate(), vars));
        n.setStatus(NotificationStatus.QUEUED);
        n.setRetryCount(0);
        n.setMaxRetries(maxRetries);
        n.setNextRetryAt(LocalDateTime.now());
        n.setVendorSubscription(subs.get(0));
        return notifications.save(n);
    }

    @Transactional(readOnly = true)
    public NotificationResponse get(Client client, Long id) {
        Notification n = notifications.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        return NotificationResponse.from(n, logs.findByNotificationIdOrderByAttemptNoAsc(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Client client, NotificationStatus status, int page, int size) {
        PageRequest pr = pageRequest(page, size);
        var result = status == null
                ? notifications.findByClientId(client.getId(), pr)
                : notifications.findByClientIdAndStatus(client.getId(), status, pr);
        return PageResponse.from(result, n -> NotificationResponse.from(n, null));
    }

    /** Manual retry: only for FAILED notifications. */
    @Transactional
    public NotificationResponse retry(Client client, Long id) {
        Notification n = notifications.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (n.getStatus() != NotificationStatus.FAILED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only FAILED notifications can be retried");
        }
        n.setStatus(NotificationStatus.QUEUED);
        n.setRetryCount(0);
        n.setLastError(null);
        n.setNextRetryAt(LocalDateTime.now());
        notifications.save(n);
        return NotificationResponse.from(n, null);
    }


    /** Edit subject/message. Only allowed while the notification is still QUEUED. */
    @Transactional
    public NotificationResponse update(Client client, Long id, UpdateNotificationRequest req) {
        Notification n = notifications.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (n.getStatus() != NotificationStatus.QUEUED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only QUEUED notifications can be edited");
        }
        if (req.subject() != null) n.setSubject(req.subject());
        n.setMessage(req.message());
        notifications.save(n);
        return NotificationResponse.from(n, null);
    }

    /** Deletes a notification together with its attempt logs. */
    @Transactional
    public void delete(Client client, Long id) {
        Notification n = notifications.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        logs.deleteByNotificationId(id);
        notifications.delete(n);
    }

    // ----- admin -----
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> adminList(NotificationStatus status, int page, int size) {
        PageRequest pr = pageRequest(page, size);
        var result = status == null ? notifications.findAll(pr) : notifications.findByStatus(status, pr);
        return PageResponse.from(result, n -> NotificationResponse.from(n, null));
    }

    @Transactional(readOnly = true)
    public NotificationResponse adminGet(Long id) {
        Notification n = notifications.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        return NotificationResponse.from(n, logs.findByNotificationIdOrderByAttemptNoAsc(id));
    }

    // ----- helpers -----
    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Replaces {{name}} placeholders. Throws 400 if a variable is missing. */
    String render(String template, Map<String, String> vars) {
        if (template == null) return null;
        Set<String> missing = new HashSet<>();
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String value = vars.get(m.group(1));
            if (value == null) {
                missing.add(m.group(1));
                value = "";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        if (!missing.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Missing template variables: " + missing);
        }
        return sb.toString();
    }
}
