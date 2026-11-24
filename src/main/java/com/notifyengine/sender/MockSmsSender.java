package com.notifyengine.sender;

import com.notifyengine.model.Notification;
import com.notifyengine.model.VendorSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockSmsSender implements VendorSender {
    private static final Logger log = LoggerFactory.getLogger(MockSmsSender.class);

    @Value("${notification.mock.failure-rate:0.4}")
    private double failureRate;

    @Override
    public boolean supports(String providerCode) {
        return "MOCK_SMS".equals(providerCode);
    }

    @Override
    public String send(Notification n, VendorSubscription sub) throws Exception {
        if (Math.random() < failureRate) {
            throw new RuntimeException("Mock SMS gateway unavailable");
        }
        log.info("[MOCK SMS] to={} text='{}'", n.getRecipient().getPhone(), n.getMessage());
        return "mock-sms-" + UUID.randomUUID();
    }
}
