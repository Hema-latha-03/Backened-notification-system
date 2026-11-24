package com.notifyengine.controller;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.model.Client;
import com.notifyengine.model.Notification;
import com.notifyengine.model.NotificationStatus;
import com.notifyengine.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public ResponseEntity<SendResponse> send(@RequestAttribute("client") Client client,
                                             @Valid @RequestBody SendRequest req) {
        Notification n = service.queue(client, req);
        return ResponseEntity.accepted().body(new SendResponse(n.getId(), n.getStatus().name()));
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return service.get(client, id);
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(@RequestAttribute("client") Client client,
                                                   @RequestParam(required = false) NotificationStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return service.list(client, status, page, size);
    }

    @PutMapping("/{id}")
    public NotificationResponse update(@RequestAttribute("client") Client client, @PathVariable Long id,
                                       @Valid @RequestBody UpdateNotificationRequest req) {
        return service.update(client, id, req);
    }

    @PostMapping("/{id}/retry")
    public NotificationResponse retry(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return service.retry(client, id);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@RequestAttribute("client") Client client, @PathVariable Long id) {
        service.delete(client, id);
        return new MessageResponse("Notification deleted along with its logs", id);
    }
}
