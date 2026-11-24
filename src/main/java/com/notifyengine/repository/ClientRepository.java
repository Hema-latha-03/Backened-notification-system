package com.notifyengine.repository;

import com.notifyengine.model.Client;
import com.notifyengine.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByApiKeyAndStatus(String apiKey, Status status);
    Optional<Client> findByApiKey(String apiKey);
}
