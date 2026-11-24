package com.notifyengine.service;

import com.notifyengine.dto.Dtos.*;
import com.notifyengine.exception.ApiException;
import com.notifyengine.model.*;
import com.notifyengine.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientSetupService {
    private final VendorRepository vendors;
    private final VendorSubscriptionRepository subscriptions;
    private final NotificationTypeRepository types;
    private final NotificationRepository notifications;

    public ClientSetupService(VendorRepository vendors, VendorSubscriptionRepository subscriptions,
                              NotificationTypeRepository types, NotificationRepository notifications) {
        this.vendors = vendors;
        this.subscriptions = subscriptions;
        this.types = types;
        this.notifications = notifications;
    }

    // ================= vendors (read only for clients) =================
    @Transactional(readOnly = true)
    public List<VendorResponse> listVendors() {
        return vendors.findByStatus(Status.ACTIVE).stream().map(VendorResponse::from).toList();
    }

    // ================= vendor subscriptions =================
    @Transactional
    public SubscriptionResponse subscribe(Client client, SubscribeRequest req) {
        Vendor vendor = vendors.findById(req.vendorId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
        if (vendor.getStatus() != Status.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vendor is not active");
        }
        if (subscriptions.existsByClientIdAndVendorId(client.getId(), vendor.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Already subscribed to this vendor");
        }
        VendorSubscription s = new VendorSubscription();
        s.setClient(client);
        s.setVendor(vendor);
        s.setConfigJson(req.configJson());
        s.setPriority(req.priority() == null ? 1 : req.priority());
        return SubscriptionResponse.from(subscriptions.save(s));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listSubscriptions(Client client) {
        return subscriptions.findByClientId(client.getId()).stream().map(SubscriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Client client, Long id) {
        return SubscriptionResponse.from(findSubscription(client, id));
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Client client, Long id, UpdateSubscriptionRequest req) {
        VendorSubscription s = findSubscription(client, id);
        if (req.configJson() != null) s.setConfigJson(req.configJson());
        if (req.priority() != null) s.setPriority(req.priority());
        if (req.active() != null) s.setActive(req.active());
        return SubscriptionResponse.from(subscriptions.save(s));
    }

    @Transactional
    public void deleteSubscription(Client client, Long id) {
        VendorSubscription s = findSubscription(client, id);
        notifications.detachSubscription(s.getId()); // old notifications keep existing, just lose the link
        subscriptions.delete(s);
    }

    private VendorSubscription findSubscription(Client client, Long id) {
        return subscriptions.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor subscription not found"));
    }

    // ================= notification types =================
    @Transactional
    public TypeResponse createType(Client client, TypeRequest req) {
        if (types.existsByClientIdAndCode(client.getId(), req.code())) {
            throw new ApiException(HttpStatus.CONFLICT, "Notification type code already exists");
        }
        NotificationType t = new NotificationType();
        t.setClient(client);
        t.setCode(req.code());
        t.setChannel(req.channel());
        t.setSubjectTemplate(req.subjectTemplate());
        t.setBodyTemplate(req.bodyTemplate());
        return TypeResponse.from(types.save(t));
    }

    @Transactional(readOnly = true)
    public List<TypeResponse> listTypes(Client client) {
        return types.findByClientId(client.getId()).stream().map(TypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TypeResponse getType(Client client, Long id) {
        return TypeResponse.from(findType(client, id));
    }

    @Transactional
    public TypeResponse updateType(Client client, Long id, UpdateTypeRequest req) {
        NotificationType t = findType(client, id);
        if (req.subjectTemplate() != null) t.setSubjectTemplate(req.subjectTemplate());
        if (req.bodyTemplate() != null) {
            if (req.bodyTemplate().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "bodyTemplate cannot be blank");
            t.setBodyTemplate(req.bodyTemplate());
        }
        if (req.active() != null) t.setActive(req.active());
        return TypeResponse.from(types.save(t));
    }

    @Transactional
    public void deleteType(Client client, Long id) {
        NotificationType t = findType(client, id);
        if (notifications.existsByTypeId(t.getId())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This type has notifications. Delete them first, or set active=false with PUT instead");
        }
        types.delete(t);
    }

    private NotificationType findType(Client client, Long id) {
        return types.findByIdAndClientId(id, client.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification type not found"));
    }
}
