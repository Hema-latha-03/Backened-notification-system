package com.notifyengine.worker;

import com.notifyengine.model.Notification;
import com.notifyengine.model.NotificationStatus;
import com.notifyengine.repository.NotificationRepository;
import com.notifyengine.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** Polls the DB for QUEUED notifications that are due and sends them. */
@Component
public class NotificationWorker {
    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);

    private final NotificationRepository notifications;
    private final DeliveryService delivery;

    @Value("${notification.worker.batch-size:20}")
    private int batchSize;

    public NotificationWorker(NotificationRepository notifications, DeliveryService delivery) {
        this.notifications = notifications;
        this.delivery = delivery;
    }

    @Scheduled(fixedDelayString = "${notification.worker.poll-ms:5000}")
    public void run() {
        List<Notification> due = notifications.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
                NotificationStatus.QUEUED, LocalDateTime.now(), PageRequest.of(0, batchSize));
        for (Notification n : due) {
            try {
                delivery.process(n.getId());
            } catch (Exception e) {
                log.error("Worker error for notification {}", n.getId(), e);
            }
        }
    }
}
