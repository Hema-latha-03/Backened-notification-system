package com.notifyengine.repository;

import com.notifyengine.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByNotificationIdOrderByAttemptNoAsc(Long notificationId);

    void deleteByNotificationId(Long notificationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificationLog l where l.notificationId in "
            + "(select n.id from Notification n where n.client.id = :clientId)")
    int deleteAllForClient(@Param("clientId") Long clientId);
}
