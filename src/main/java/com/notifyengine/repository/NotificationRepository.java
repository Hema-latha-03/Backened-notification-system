package com.notifyengine.repository;

import com.notifyengine.model.Notification;
import com.notifyengine.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // used by the worker
    List<Notification> findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            NotificationStatus status, LocalDateTime now, Pageable pageable);

    Optional<Notification> findByIdAndClientId(Long id, Long clientId);
    Page<Notification> findByClientId(Long clientId, Pageable pageable);
    Page<Notification> findByClientIdAndStatus(Long clientId, NotificationStatus status, Pageable pageable);
    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    List<Notification> findByRecipientId(Long recipientId);
    boolean existsByTypeId(Long typeId);
    void deleteByClientId(Long clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.vendorSubscription = null where n.vendorSubscription.id = :subId")
    int detachSubscription(@Param("subId") Long subscriptionId);
}
