package com.notifyengine.service;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.exception.ApiException;
import com.notifyengine.model.Client;
import com.notifyengine.model.Recipient;
import com.notifyengine.model.UserEvent;
import com.notifyengine.repository.RecipientRepository;
import com.notifyengine.repository.UserEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** CRUD for user events such as ORDER_PLACED or ORDER_DELETED. */
@Service
public class UserEventService {
    private final UserEventRepository events;
    private final RecipientRepository recipients;

    public UserEventService(UserEventRepository events, RecipientRepository recipients) {
        this.events = events;
        this.recipients = recipients;
    }

    @Transactional
    public EventResponse create(Client client, EventRequest req) {
        Recipient r = recipients.findByIdAndClientId(req.recipientId(), client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        UserEvent e = new UserEvent();
        e.setClient(client);
        e.setRecipient(r);
        e.setEventType(req.eventType().trim().toUpperCase());
        e.setReferenceId(req.referenceId());
        e.setDetails(req.details());
        return EventResponse.from(events.save(e));
    }

    @Transactional(readOnly = true)
    public EventResponse get(Client client, Long id) {
        return EventResponse.from(find(client, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> list(Client client, Long recipientId, String eventType, int page, int size) {
        String type = (eventType == null || eventType.isBlank()) ? null : eventType.trim().toUpperCase();
        return PageResponse.from(events.search(client.getId(), recipientId, type, Paging.of(page, size)),
                EventResponse::from);
    }

    @Transactional
    public EventResponse update(Client client, Long id, UpdateEventRequest req) {
        UserEvent e = find(client, id);
        e.setEventType(req.eventType().trim().toUpperCase());
        e.setReferenceId(req.referenceId());
        e.setDetails(req.details());
        e.setUpdatedAt(LocalDateTime.now());
        return EventResponse.from(events.save(e));
    }

    @Transactional
    public void delete(Client client, Long id) {
        events.delete(find(client, id));
    }

    private UserEvent find(Client client, Long id) {
        return events.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
    }
}
