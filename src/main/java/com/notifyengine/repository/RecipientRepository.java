package com.notifyengine.repository;

import com.notifyengine.model.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByClientIdAndExternalRef(Long clientId, String externalRef);
    Optional<Recipient> findByIdAndClientId(Long id, Long clientId);
    Page<Recipient> findByClientId(Long clientId, Pageable pageable);
    void deleteByClientId(Long clientId);
}
