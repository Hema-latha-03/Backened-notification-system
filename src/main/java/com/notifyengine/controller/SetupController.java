package com.notifyengine.controller;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.model.Client;
import com.notifyengine.service.ClientSetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Vendor subscriptions and notification types of the calling client (X-API-KEY). */
@RestController
@RequestMapping("/api/v1")
public class SetupController {
    private final ClientSetupService setup;

    public SetupController(ClientSetupService setup) {
        this.setup = setup;
    }

    // ---- vendors (read only) ----
    @GetMapping("/vendors")
    public List<VendorResponse> vendors() {
        return setup.listVendors();
    }

    // ---- vendor subscriptions ----
    @PostMapping("/vendor-subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse subscribe(@RequestAttribute("client") Client client,
                                          @Valid @RequestBody SubscribeRequest req) {
        return setup.subscribe(client, req);
    }

    @GetMapping("/vendor-subscriptions")
    public List<SubscriptionResponse> subscriptions(@RequestAttribute("client") Client client) {
        return setup.listSubscriptions(client);
    }

    @GetMapping("/vendor-subscriptions/{id}")
    public SubscriptionResponse subscription(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return setup.getSubscription(client, id);
    }

    @PutMapping("/vendor-subscriptions/{id}")
    public SubscriptionResponse updateSubscription(@RequestAttribute("client") Client client, @PathVariable Long id,
                                                   @RequestBody UpdateSubscriptionRequest req) {
        return setup.updateSubscription(client, id, req);
    }

    @DeleteMapping("/vendor-subscriptions/{id}")
    public MessageResponse deleteSubscription(@RequestAttribute("client") Client client, @PathVariable Long id) {
        setup.deleteSubscription(client, id);
        return new MessageResponse("Vendor subscription deleted", id);
    }

    // ---- notification types ----
    @PostMapping("/notification-types")
    @ResponseStatus(HttpStatus.CREATED)
    public TypeResponse createType(@RequestAttribute("client") Client client, @Valid @RequestBody TypeRequest req) {
        return setup.createType(client, req);
    }

    @GetMapping("/notification-types")
    public List<TypeResponse> types(@RequestAttribute("client") Client client) {
        return setup.listTypes(client);
    }

    @GetMapping("/notification-types/{id}")
    public TypeResponse type(@RequestAttribute("client") Client client, @PathVariable Long id) {
        return setup.getType(client, id);
    }

    @PutMapping("/notification-types/{id}")
    public TypeResponse updateType(@RequestAttribute("client") Client client, @PathVariable Long id,
                                   @RequestBody UpdateTypeRequest req) {
        return setup.updateType(client, id, req);
    }

    @DeleteMapping("/notification-types/{id}")
    public MessageResponse deleteType(@RequestAttribute("client") Client client, @PathVariable Long id) {
        setup.deleteType(client, id);
        return new MessageResponse("Notification type deleted", id);
    }
}
