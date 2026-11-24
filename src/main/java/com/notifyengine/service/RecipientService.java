package com.notifyengine.service;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.exception.ApiException;
import com.notifyengine.model.Client;
import com.notifyengine.model.Notification;
import com.notifyengine.model.Recipient;
import com.notifyengine.repository.NotificationLogRepository;
import com.notifyengine.repository.NotificationRepository;
import com.notifyengine.repository.RecipientRepository;
import com.notifyengine.repository.UserEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD for the client's users. */
@Service
public class RecipientService {
    private final RecipientRepository recipients;
    private final NotificationRepository notifications;
    private final NotificationLogRepository logs;
    private final UserEventRepository events;

    public RecipientService(RecipientRepository recipients, NotificationRepository notifications,
                            NotificationLogRepository logs, UserEventRepository events) {
        this.recipients = recipients;
        this.notifications = notifications;
        this.logs = logs;
        this.events = events;
    }

    @Transactional
    public RecipientResponse create(Client client, RecipientRequest req) {
        if (recipients.findByClientIdAndExternalRef(client.getId(), req.externalRef()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "A user with this externalRef already exists");
        }
        Recipient r = new Recipient();
        r.setClient(client);
        apply(r, req);
        return RecipientResponse.from(recipients.save(r));
    }

    @Transactional(readOnly = true)
    public RecipientResponse get(Client client, Long id) {
        return RecipientResponse.from(find(client, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<RecipientResponse> list(Client client, int page, int size) {
        return PageResponse.from(recipients.findByClientId(client.getId(), Paging.of(page, size)),
                RecipientResponse::from);
    }

    @Transactional
    public RecipientResponse update(Client client, Long id, RecipientRequest req) {
        Recipient r = find(client, id);
        boolean taken = recipients.findByClientIdAndExternalRef(client.getId(), req.externalRef())
                .filter(other -> !other.getId().equals(id)).isPresent();
        if (taken) {
            throw new ApiException(HttpStatus.CONFLICT, "Another user already uses this externalRef");
        }
        apply(r, req);
        return RecipientResponse.from(recipients.save(r));
    }

    /** Deletes the user together with their notifications, attempt logs and events. */
    @Transactional
    public void delete(Client client, Long id) {
        Recipient r = find(client, id);
        for (Notification n : notifications.findByRecipientId(id)) {
            logs.deleteByNotificationId(n.getId());
            notifications.delete(n);
        }
        events.deleteByRecipientId(id);
        recipients.delete(r);
    }

    private Recipient find(Client client, Long id) {
        return recipients.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void apply(Recipient r, RecipientRequest req) {
        r.setExternalRef(req.externalRef());
        r.setName(req.name());
        r.setEmail(req.email());
        r.setPhone(req.phone());
        r.setEmailOptIn(req.emailOptIn() == null || req.emailOptIn());
        r.setSmsOptIn(req.smsOptIn() == null || req.smsOptIn());
    }
}
