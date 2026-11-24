package com.notifyengine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_log", indexes = @Index(name = "idx_log_notification", columnList = "notification_id"))
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "vendor_subscription_id")
    private Long vendorSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Column(name = "vendor_response", length = 500)
    private String vendorResponse;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public int getAttemptNo() { return attemptNo; }
    public void setAttemptNo(int attemptNo) { this.attemptNo = attemptNo; }
    public Long getVendorSubscriptionId() { return vendorSubscriptionId; }
    public void setVendorSubscriptionId(Long id) { this.vendorSubscriptionId = id; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public String getVendorResponse() { return vendorResponse; }
    public void setVendorResponse(String vendorResponse) { this.vendorResponse = vendorResponse; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
}
