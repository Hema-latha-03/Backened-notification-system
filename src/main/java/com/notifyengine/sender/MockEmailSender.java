package com.notifyengine.sender;

import com.notifyengine.model.Notification;
import com.notifyengine.model.VendorSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fake email vendor. To use a real one (SendGrid, Gmail SMTP), create another class
 * implementing VendorSender with its own provider code, and add a vendor row for it.
 */
@Component
public class MockEmailSender implements VendorSender {
    private static final Logger log = LoggerFactory.getLogger(MockEmailSender.class);

    @Value("${notification.mock.failure-rate:0.4}")
    private double failureRate;

    @Override
    public boolean supports(String providerCode) {
        return "MOCK_EMAIL".equals(providerCode);
    }

    @Override
    public String send(Notification n, VendorSubscription sub) throws Exception {
        if (Math.random() < failureRate) {
            throw new RuntimeException("Mock SMTP timeout");
        }
        log.info("[MOCK EMAIL] to={} subject='{}' body='{}'", n.getRecipient().getEmail(), n.getSubject(), n.getMessage());
        return "mock-email-" + UUID.randomUUID();
    }
}
