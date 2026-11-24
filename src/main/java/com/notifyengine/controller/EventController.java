package com.notifyengine.controller;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.model.Client;
import com.notifyengine.service.UserEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CRUD for user events (ORDER_PLACED, ORDER_DELETED, ...). */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final UserEventService service;

    public EventController(UserEventService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@RequestAttribute("client") Client client, @Valid @RequestBody EventRequest req) {
        return service.create(client, req);
    }

    @GetMapping("/{id}")
    public EventResponse get(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return service.get(client, id);
    }

    /** e.g. GET /api/v1/events?recipientId=1&eventType=ORDER_PLACED */
    @GetMapping
    public PageResponse<EventResponse> list(@RequestAttribute("client") Client client,
                                            @RequestParam(required = false) Long recipientId,
                                            @RequestParam(required = false) String eventType,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return service.list(client, recipientId, eventType, page, size);
    }

    @PutMapping("/{id}")
    public EventResponse update(@RequestAttribute("client") Client client, @PathVariable Long id,
                                @Valid @RequestBody UpdateEventRequest req) {
        return service.update(client, id, req);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@RequestAttribute("client") Client client, @PathVariable Long id) {
        service.delete(client, id);
        return new MessageResponse("Event deleted", id);
    }
}
