package com.notifyengine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recipient",
        uniqueConstraints = @UniqueConstraint(name = "uk_client_extref", columnNames = {"client_id", "external_ref"}))
public class Recipient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "external_ref", nullable = false, length = 100)
    private String externalRef;

    private String name;
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "email_opt_in", nullable = false)
    private boolean emailOptIn = true;

    @Column(name = "sms_opt_in", nullable = false)
    private boolean smsOptIn = true;

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isEmailOptIn() { return emailOptIn; }
    public void setEmailOptIn(boolean emailOptIn) { this.emailOptIn = emailOptIn; }
    public boolean isSmsOptIn() { return smsOptIn; }
    public void setSmsOptIn(boolean smsOptIn) { this.smsOptIn = smsOptIn; }
}
