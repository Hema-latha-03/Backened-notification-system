package com.notifyengine.repository;

import com.notifyengine.model.UserEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    Optional<UserEvent> findByIdAndClientId(Long id, Long clientId);

    @Query("select e from UserEvent e where e.client.id = :cid "
            + "and (:rid is null or e.recipient.id = :rid) "
            + "and (:type is null or e.eventType = :type)")
    Page<UserEvent> search(@Param("cid") Long clientId, @Param("rid") Long recipientId,
                           @Param("type") String eventType, Pageable pageable);

    void deleteByRecipientId(Long recipientId);
    void deleteByClientId(Long clientId);
}
