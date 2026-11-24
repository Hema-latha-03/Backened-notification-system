package com.notifyengine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A thing that happened to a user in the client app, e.g. ORDER_PLACED, ORDER_DELETED. */
@Entity
@Table(name = "user_event", indexes = {
        @Index(name = "idx_event_client_recipient", columnList = "client_id,recipient_id"),
        @Index(name = "idx_event_type", columnList = "event_type")
})
public class UserEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Recipient getRecipient() { return recipient; }
    public void setRecipient(Recipient recipient) { this.recipient = recipient; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
