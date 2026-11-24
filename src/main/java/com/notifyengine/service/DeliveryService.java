package com.notifyengine.service;

import com.notifyengine.model.*;
import com.notifyengine.repository.NotificationLogRepository;
import com.notifyengine.repository.NotificationRepository;
import com.notifyengine.repository.VendorSubscriptionRepository;
import com.notifyengine.sender.VendorSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Performs ONE delivery attempt for a notification, writes the log row and decides retry vs FAILED. */
@Service
public class DeliveryService {
    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final NotificationRepository notifications;
    private final NotificationLogRepository logs;
    private final VendorSubscriptionRepository subscriptions;
    private final List<VendorSender> senders;

    @Value("${notification.retry.base-delay-seconds:10}")
    private long baseDelaySeconds;

    public DeliveryService(NotificationRepository notifications, NotificationLogRepository logs,
                           VendorSubscriptionRepository subscriptions, List<VendorSender> senders) {
        this.notifications = notifications;
        this.logs = logs;
        this.subscriptions = subscriptions;
        this.senders = senders;
    }

    @Transactional
    public void process(Long notificationId) {
        Notification n = notifications.findById(notificationId).orElse(null);
        if (n == null || n.getStatus() != NotificationStatus.QUEUED) return;

        int attemptNo = n.getRetryCount() + 1;
        NotificationLog entry = new NotificationLog();
        entry.setNotificationId(n.getId());
        entry.setAttemptNo(attemptNo);

        try {
            List<VendorSubscription> subs = subscriptions
                    .findByClientIdAndActiveTrueAndVendorChannelAndVendorStatusOrderByPriorityAsc(
                            n.getClient().getId(), n.getChannel(), Status.ACTIVE);
            if (subs.isEmpty()) throw new IllegalStateException("No active vendor subscription");

            // vendor failover: attempt 1 -> priority 1 vendor, attempt 2 -> next vendor, ...
            VendorSubscription sub = subs.get(n.getRetryCount() % subs.size());
            n.setVendorSubscription(sub);
            entry.setVendorSubscriptionId(sub.getId());

            String code = sub.getVendor().getProviderCode();
            VendorSender sender = senders.stream().filter(s -> s.supports(code)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("No sender implemented for provider " + code));

            String response = sender.send(n, sub);

            entry.setStatus(AttemptStatus.SUCCESS);
            entry.setVendorResponse(cut(response));
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
            n.setLastError(null);
            log.info("Notification {} SENT on attempt {}", n.getId(), attemptNo);
        } catch (Exception e) {
            String msg = cut(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            entry.setStatus(AttemptStatus.FAILED);
            entry.setErrorMessage(msg);
            n.setLastError(msg);

            if (n.getRetryCount() >= n.getMaxRetries()) {
                n.setStatus(NotificationStatus.FAILED);
                log.warn("Notification {} permanently FAILED after {} attempts: {}", n.getId(), attemptNo, msg);
            } else {
                n.setRetryCount(n.getRetryCount() + 1);
                long delay = baseDelaySeconds * (1L << (n.getRetryCount() - 1)); // 10s, 20s, 40s, 80s
                n.setNextRetryAt(LocalDateTime.now().plusSeconds(delay));
                log.warn("Notification {} attempt {} failed ({}). Retry #{} in {}s",
                        n.getId(), attemptNo, msg, n.getRetryCount(), delay);
            }
        }
        logs.save(entry);
        notifications.save(n);
    }

    private String cut(String s) {
        return s == null ? null : (s.length() > 480 ? s.substring(0, 480) : s);
    }
}
