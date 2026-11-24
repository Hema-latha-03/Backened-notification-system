package com.notifyengine.controller;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.model.NotificationStatus;
import com.notifyengine.model.Status;
import com.notifyengine.service.AdminService;
import com.notifyengine.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin APIs. Every call needs the X-ADMIN-KEY header. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final NotificationService notifications;

    public AdminController(AdminService admin, NotificationService notifications) {
        this.admin = admin;
        this.notifications = notifications;
    }

    // ---- clients ----
    @PostMapping("/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse createClient(@Valid @RequestBody CreateClientRequest req) {
        return admin.createClient(req);
    }

    @GetMapping("/clients")
    public List<ClientResponse> listClients() {
        return admin.listClients();
    }

    @GetMapping("/clients/{id}")
    public ClientResponse getClient(@PathVariable Long id) {
        return admin.getClient(id);
    }

    @PutMapping("/clients/{id}")
    public ClientResponse updateClient(@PathVariable Long id, @Valid @RequestBody UpdateClientRequest req) {
        return admin.updateClient(id, req);
    }

    @PatchMapping("/clients/{id}/status")
    public ClientResponse setClientStatus(@PathVariable Long id, @RequestParam Status value) {
        return admin.setClientStatus(id, value);
    }

    @DeleteMapping("/clients/{id}")
    public MessageResponse deleteClient(@PathVariable Long id) {
        admin.deleteClient(id);
        return new MessageResponse("Client deleted with all its data", id);
    }

    // ---- vendors ----
    @PostMapping("/vendors")
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse createVendor(@Valid @RequestBody CreateVendorRequest req) {
        return admin.createVendor(req);
    }

    @GetMapping("/vendors")
    public List<VendorResponse> listVendors() {
        return admin.listVendors();
    }

    @GetMapping("/vendors/{id}")
    public VendorResponse getVendor(@PathVariable Long id) {
        return admin.getVendor(id);
    }

    @PutMapping("/vendors/{id}")
    public VendorResponse updateVendor(@PathVariable Long id, @Valid @RequestBody UpdateVendorRequest req) {
        return admin.updateVendor(id, req);
    }

    @DeleteMapping("/vendors/{id}")
    public MessageResponse deleteVendor(@PathVariable Long id) {
        admin.deleteVendor(id);
        return new MessageResponse("Vendor deleted", id);
    }

    // ---- notifications (all clients, read only) ----
    @GetMapping("/notifications")
    public PageResponse<NotificationResponse> notifications(@RequestParam(required = false) NotificationStatus status,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return notifications.adminList(status, page, size);
    }

    @GetMapping("/notifications/{id}")
    public NotificationResponse notification(@PathVariable Long id) {
        return notifications.adminGet(id);
    }
}
