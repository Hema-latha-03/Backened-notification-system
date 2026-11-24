package com.notifyengine.controller;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.model.Client;
import com.notifyengine.service.RecipientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CRUD for the client's users (the people who receive notifications). */
@RestController
@RequestMapping("/api/v1/recipients")
public class RecipientController {
    private final RecipientService service;

    public RecipientController(RecipientService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipientResponse create(@RequestAttribute("client") Client client,
                                    @Valid @RequestBody RecipientRequest req) {
        return service.create(client, req);
    }

    @GetMapping("/{id}")
    public RecipientResponse get(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return service.get(client, id);
    }

    @GetMapping
    public PageResponse<RecipientResponse> list(@RequestAttribute("client") Client client,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return service.list(client, page, size);
    }

    @PutMapping("/{id}")
    public RecipientResponse update(@RequestAttribute("client") Client client, @PathVariable Long id,
                                    @Valid @RequestBody RecipientRequest req) {
        return service.update(client, id, req);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@RequestAttribute("client") Client client, @PathVariable Long id) {
        service.delete(client, id);
        return new MessageResponse("User deleted along with their notifications and events", id);
    }
}
