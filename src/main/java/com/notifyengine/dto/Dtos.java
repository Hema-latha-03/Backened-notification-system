package com.notifyengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.notifyengine.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Dtos {
    private Dtos() {}

    public record MessageResponse(String message, Long id) {}

    // ---------- admin: clients & vendors ----------
    public record CreateClientRequest(@NotBlank String name) {}
    public record UpdateClientRequest(@NotBlank String name) {}

    public record ClientResponse(Long id, String name, String apiKey, Status status) {
        public static ClientResponse from(Client c) {
            return new ClientResponse(c.getId(), c.getName(), c.getApiKey(), c.getStatus());
        }
    }

    public record CreateVendorRequest(@NotBlank String name, @NotNull Channel channel, @NotBlank String providerCode) {}
    public record UpdateVendorRequest(@NotBlank String name, Status status) {}

    public record VendorResponse(Long id, String name, Channel channel, String providerCode, Status status) {
        public static VendorResponse from(Vendor v) {
            return new VendorResponse(v.getId(), v.getName(), v.getChannel(), v.getProviderCode(), v.getStatus());
        }
    }

    // ---------- vendor subscriptions ----------
    public record SubscribeRequest(@NotNull Long vendorId, String configJson, Integer priority) {}
    public record UpdateSubscriptionRequest(String configJson, Integer priority, Boolean active) {}

    public record SubscriptionResponse(Long id, Long vendorId, String vendorName, Channel channel,
                                       String configJson, int priority, boolean active) {
        public static SubscriptionResponse from(VendorSubscription s) {
            return new SubscriptionResponse(s.getId(), s.getVendor().getId(), s.getVendor().getName(),
                    s.getVendor().getChannel(), s.getConfigJson(), s.getPriority(), s.isActive());
        }
    }

    // ---------- recipients (the client's users) ----------
    public record RecipientRequest(@NotBlank String externalRef, String name, String email, String phone,
                                   Boolean emailOptIn, Boolean smsOptIn) {}

    public record RecipientResponse(Long id, String externalRef, String name, String email, String phone,
                                    boolean emailOptIn, boolean smsOptIn) {
        public static RecipientResponse from(Recipient r) {
            return new RecipientResponse(r.getId(), r.getExternalRef(), r.getName(), r.getEmail(), r.getPhone(),
                    r.isEmailOptIn(), r.isSmsOptIn());
        }
    }

    // ---------- notification types ----------
    public record TypeRequest(@NotBlank String code, @NotNull Channel channel, String subjectTemplate,
                              @NotBlank String bodyTemplate) {}
    public record UpdateTypeRequest(String subjectTemplate, String bodyTemplate, Boolean active) {}

    public record TypeResponse(Long id, String code, Channel channel, String subjectTemplate, String bodyTemplate, boolean active) {
        public static TypeResponse from(NotificationType t) {
            return new TypeResponse(t.getId(), t.getCode(), t.getChannel(), t.getSubjectTemplate(),
                    t.getBodyTemplate(), t.isActive());
        }
    }

    // ---------- user events (order placed, order deleted ...) ----------
    public record EventRequest(@NotNull Long recipientId, @NotBlank String eventType, String referenceId, String details) {}
    public record UpdateEventRequest(@NotBlank String eventType, String referenceId, String details) {}

    public record EventResponse(Long id, Long recipientId, String eventType, String referenceId, String details,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static EventResponse from(UserEvent e) {
            return new EventResponse(e.getId(), e.getRecipient().getId(), e.getEventType(), e.getReferenceId(),
                    e.getDetails(), e.getCreatedAt(), e.getUpdatedAt());
        }
    }

    // ---------- notifications ----------
    public record SendRequest(@NotBlank String recipientRef, @NotBlank String typeCode, Map<String, String> variables) {}
    public record UpdateNotificationRequest(String subject, @NotBlank String message) {}
    public record SendResponse(Long notificationId, String status) {}

    public record LogResponse(int attemptNo, String status, Long vendorSubscriptionId, String vendorResponse,
                              String errorMessage, LocalDateTime attemptedAt) {
        public static LogResponse from(NotificationLog l) {
            return new LogResponse(l.getAttemptNo(), l.getStatus().name(), l.getVendorSubscriptionId(),
                    l.getVendorResponse(), l.getErrorMessage(), l.getAttemptedAt());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotificationResponse(Long id, String recipientRef, String typeCode, String channel, String status,
                                       int retryCount, int maxRetries, String subject, String message,
                                       String lastError, LocalDateTime createdAt, LocalDateTime nextRetryAt,
                                       LocalDateTime sentAt, List<LogResponse> logs) {
        public static NotificationResponse from(Notification n, List<NotificationLog> logs) {
            return new NotificationResponse(n.getId(), n.getRecipient().getExternalRef(), n.getType().getCode(),
                    n.getChannel().name(), n.getStatus().name(), n.getRetryCount(), n.getMaxRetries(),
                    n.getSubject(), n.getMessage(), n.getLastError(), n.getCreatedAt(), n.getNextRetryAt(),
                    n.getSentAt(), logs == null ? null : logs.stream().map(LogResponse::from).toList());
        }
    }

    public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
        public static <E, T> PageResponse<T> from(Page<E> p, Function<E, T> mapper) {
            return new PageResponse<>(p.getContent().stream().map(mapper).toList(),
                    p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
