package com.notifyengine.repository;

import com.notifyengine.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
    Optional<NotificationType> findByClientIdAndCodeAndActiveTrue(Long clientId, String code);
    Optional<NotificationType> findByIdAndClientId(Long id, Long clientId);
    List<NotificationType> findByClientId(Long clientId);
    boolean existsByClientIdAndCode(Long clientId, String code);
    void deleteByClientId(Long clientId);
}
