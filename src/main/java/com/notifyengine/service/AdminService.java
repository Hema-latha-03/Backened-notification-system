package com.notifyengine.service;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.exception.ApiException;
import com.notifyengine.model.*;
import com.notifyengine.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final ClientRepository clients;
    private final VendorRepository vendors;
    private final VendorSubscriptionRepository subscriptions;
    private final RecipientRepository recipients;
    private final NotificationTypeRepository types;
    private final UserEventRepository events;
    private final NotificationRepository notifications;
    private final NotificationLogRepository logs;

    public AdminService(ClientRepository clients, VendorRepository vendors,
                        VendorSubscriptionRepository subscriptions, RecipientRepository recipients,
                        NotificationTypeRepository types, UserEventRepository events,
                        NotificationRepository notifications, NotificationLogRepository logs) {
        this.clients = clients;
        this.vendors = vendors;
        this.subscriptions = subscriptions;
        this.recipients = recipients;
        this.types = types;
        this.events = events;
        this.notifications = notifications;
        this.logs = logs;
    }

    // ================= clients =================
    @Transactional
    public ClientResponse createClient(CreateClientRequest req) {
        Client c = new Client();
        c.setName(req.name());
        c.setApiKey("ak_" + UUID.randomUUID().toString().replace("-", ""));
        return ClientResponse.from(clients.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listClients() {
        return clients.findAll().stream().map(ClientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(Long id) {
        return ClientResponse.from(findClient(id));
    }

    @Transactional
    public ClientResponse updateClient(Long id, UpdateClientRequest req) {
        Client c = findClient(id);
        c.setName(req.name());
        return ClientResponse.from(clients.save(c));
    }

    @Transactional
    public ClientResponse setClientStatus(Long id, Status status) {
        Client c = findClient(id);
        c.setStatus(status);
        return ClientResponse.from(clients.save(c));
    }

    /** Deletes the client and ALL its data (users, events, types, subscriptions, notifications, logs). */
    @Transactional
    public void deleteClient(Long id) {
        Client c = findClient(id);
        logs.deleteAllForClient(id);
        notifications.deleteByClientId(id);
        events.deleteByClientId(id);
        recipients.deleteByClientId(id);
        types.deleteByClientId(id);
        subscriptions.deleteByClientId(id);
        clients.delete(c);
    }

    private Client findClient(Long id) {
        return clients.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    // ================= vendors =================
    @Transactional
    public VendorResponse createVendor(CreateVendorRequest req) {
        if (vendors.existsByProviderCode(req.providerCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "Provider code already exists");
        }
        Vendor v = new Vendor();
        v.setName(req.name());
        v.setChannel(req.channel());
        v.setProviderCode(req.providerCode());
        return VendorResponse.from(vendors.save(v));
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> listVendors() {
        return vendors.findAll().stream().map(VendorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(Long id) {
        return VendorResponse.from(findVendor(id));
    }

    @Transactional
    public VendorResponse updateVendor(Long id, UpdateVendorRequest req) {
        Vendor v = findVendor(id);
        v.setName(req.name());
        if (req.status() != null) v.setStatus(req.status());
        return VendorResponse.from(vendors.save(v));
    }

    @Transactional
    public void deleteVendor(Long id) {
        Vendor v = findVendor(id);
        if (subscriptions.existsByVendorId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Vendor has client subscriptions. Set status=INACTIVE with PUT instead");
        }
        vendors.delete(v);
    }

    private Vendor findVendor(Long id) {
        return vendors.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
    }
}
