package com.notifyengine.repository;

import com.notifyengine.model.Status;
import com.notifyengine.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByStatus(Status status);
    boolean existsByProviderCode(String providerCode);
}
