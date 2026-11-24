package com.notifyengine.repository;

import com.notifyengine.model.Channel;
import com.notifyengine.model.Status;
import com.notifyengine.model.VendorSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VendorSubscriptionRepository extends JpaRepository<VendorSubscription, Long> {
    boolean existsByClientIdAndVendorId(Long clientId, Long vendorId);
    boolean existsByVendorId(Long vendorId);
    List<VendorSubscription> findByClientId(Long clientId);
    Optional<VendorSubscription> findByIdAndClientId(Long id, Long clientId);
    void deleteByClientId(Long clientId);

    // active subscriptions of a client for one channel, best priority first
    List<VendorSubscription> findByClientIdAndActiveTrueAndVendorChannelAndVendorStatusOrderByPriorityAsc(
            Long clientId, Channel channel, Status vendorStatus);
}
