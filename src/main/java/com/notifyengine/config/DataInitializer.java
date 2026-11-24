package com.notifyengine.config;

import com.notifyengine.model.*;
import com.notifyengine.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds demo data on first run so you can test immediately. */
@Component
public class DataInitializer implements CommandLineRunner {
    public static final String DEMO_API_KEY = "demo-api-key-123";

    private final VendorRepository vendors;
    private final ClientRepository clients;
    private final VendorSubscriptionRepository subscriptions;
    private final RecipientRepository recipients;
    private final NotificationTypeRepository types;

    public DataInitializer(VendorRepository vendors, ClientRepository clients,
                           VendorSubscriptionRepository subscriptions,
                           RecipientRepository recipients, NotificationTypeRepository types) {
        this.vendors = vendors;
        this.clients = clients;
        this.subscriptions = subscriptions;
        this.recipients = recipients;
        this.types = types;
    }

    @Override
    public void run(String... args) {
        if (clients.findByApiKey(DEMO_API_KEY).isPresent()) return;

        Vendor email = vendor("Mock Email Provider", Channel.EMAIL, "MOCK_EMAIL");
        Vendor sms = vendor("Mock SMS Provider", Channel.SMS, "MOCK_SMS");

        Client c = new Client();
        c.setName("Demo Shop");
        c.setApiKey(DEMO_API_KEY);
        c = clients.save(c);

        subscribe(c, email);
        subscribe(c, sms);

        Recipient r = new Recipient();
        r.setClient(c);
        r.setExternalRef("user-101");
        r.setName("Demo User");
        r.setEmail("demo.user@example.com");
        r.setPhone("+910000000000");
        recipients.save(r);

        type(c, "WELCOME_EMAIL", Channel.EMAIL, "Welcome, {{name}}!",
                "Hi {{name}}, thanks for joining {{shop}}.");
        type(c, "OTP_SMS", Channel.SMS, null,
                "Your OTP is {{otp}}. It expires in 5 minutes.");

        System.out.println(">>> Demo client ready. X-API-KEY = " + DEMO_API_KEY + ", recipient = user-101");
    }

    private Vendor vendor(String name, Channel channel, String code) {
        return vendors.findAll().stream().filter(v -> v.getProviderCode().equals(code)).findFirst().orElseGet(() -> {
            Vendor v = new Vendor();
            v.setName(name);
            v.setChannel(channel);
            v.setProviderCode(code);
            return vendors.save(v);
        });
    }

    private void subscribe(Client c, Vendor v) {
        VendorSubscription s = new VendorSubscription();
        s.setClient(c);
        s.setVendor(v);
        s.setPriority(1);
        s.setConfigJson("{}");
        subscriptions.save(s);
    }

    private void type(Client c, String code, Channel ch, String subject, String body) {
        NotificationType t = new NotificationType();
        t.setClient(c);
        t.setCode(code);
        t.setChannel(ch);
        t.setSubjectTemplate(subject);
        t.setBodyTemplate(body);
        types.save(t);
    }
}
