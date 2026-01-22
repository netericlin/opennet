
package com.opennet.test.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opennet.test.exception.MessagingException;
import com.opennet.test.exception.ResourceNotFoundException;
import com.opennet.test.model.Notification;
import com.opennet.test.repository.NotificationRepository;
import com.opennet.test.services.messaging.RocketMQService;
import com.opennet.test.services.redis.NotificationRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String ROCKETMQ_TOPIC = "notification-topic";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    private NotificationRedisService notificationRedisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(ZonedDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);

        try {
            notificationRedisService.addNotificationToRecentCache(savedNotification);
            String messageBody = objectMapper.writeValueAsString(savedNotification);
            rocketMQService.sendMessage(ROCKETMQ_TOPIC, messageBody);
        } catch (Exception e) {
            notificationRedisService.removeNotificationFromRecentCache(savedNotification);
            logger.atError().log(
                    "Error sending notification message for notification with id {} after transaction commit",
                    savedNotification.getId());
            throw new MessagingException("Error sending notification message", e);
        }

        return savedNotification;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        Notification notification = notificationRedisService.getNotificationFromCache(id);
        if (notification == null) {
            notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
            notificationRedisService.cacheNotification(notification);
        }
        return notification;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(int limit) {
        List<Notification> notifications = notificationRedisService.getRecentNotificationsFromCache();
        if (notifications == null || notifications.isEmpty()) {
            notifications = notificationRepository.findAll(
                    PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
            notificationRedisService.cacheRecentNotifications(notifications);
        }
        return notifications;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public Notification updateNotification(Long id, Notification updatedNotification) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));

        existingNotification.setSubject(updatedNotification.getSubject());
        existingNotification.setContent(updatedNotification.getContent());

        Notification savedNotification = notificationRepository.save(existingNotification);

        notificationRedisService.removeNotificationFromRecentCache(existingNotification);
        notificationRedisService.cacheNotification(savedNotification);
        notificationRedisService.addNotificationToRecentCache(savedNotification);

        return savedNotification;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    public void deleteNotification(Long id) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));

        notificationRepository.deleteById(id);
        notificationRedisService.evictNotificationFromCache(id);
        notificationRedisService.removeNotificationFromRecentCache(existingNotification);
    }
}
