package com.opennet.test.services.redis;

import com.opennet.test.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationRedisServiceImpl implements NotificationRedisService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRedisServiceImpl.class);
    private static final String RECENT_NOTIFICATIONS_KEY = "recent_notifications";
    private static final String NOTIFICATION_KEY_PREFIX = "notification:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("null")
    @Override
    public void addNotificationToRecentCache(Notification notification) {
        redisTemplate.opsForList().leftPush(RECENT_NOTIFICATIONS_KEY, notification);
        redisTemplate.opsForList().trim(RECENT_NOTIFICATIONS_KEY, 0, 9);
    }

    @SuppressWarnings("null")
    @Override
    public void cacheRecentNotifications(List<Notification> notifications) {
        redisTemplate.delete(RECENT_NOTIFICATIONS_KEY);
        redisTemplate.opsForList().rightPushAll(RECENT_NOTIFICATIONS_KEY, notifications.toArray());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Notification> getRecentNotificationsFromCache() {
        try {
            return (List<Notification>) (List<?>) redisTemplate.opsForList().range(RECENT_NOTIFICATIONS_KEY, 0, -1);
        } catch (Exception e) {
            logger.error("Failed to deserialize recent notifications. Clearing cache.", e);
            redisTemplate.delete(RECENT_NOTIFICATIONS_KEY);
            return null;
        }
    }

    @Override
    public void clearRecentNotificationsCache() {
        redisTemplate.delete(RECENT_NOTIFICATIONS_KEY);
    }

    @SuppressWarnings("null")
    @Override
    public void removeNotificationFromRecentCache(Notification notification) {
        List<Notification> notifications = getRecentNotificationsFromCache();
        if (notifications != null && !notifications.isEmpty()) {
            boolean removed = notifications.removeIf(n -> n.getId().equals(notification.getId()));
            if (removed) {
                redisTemplate.delete(RECENT_NOTIFICATIONS_KEY);
                if (!notifications.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(RECENT_NOTIFICATIONS_KEY, notifications.toArray());
                }
            }
        }
    }

    @Override
    public Notification getNotificationFromCache(Long id) {
        return (Notification) redisTemplate.opsForValue().get(NOTIFICATION_KEY_PREFIX + id);
    }

    @Override
    public void cacheNotification(Notification notification) {
        redisTemplate.opsForValue().set(NOTIFICATION_KEY_PREFIX + notification.getId(), notification, 1,
                TimeUnit.HOURS);
    }

    @Override
    public void evictNotificationFromCache(Long id) {
        redisTemplate.delete(NOTIFICATION_KEY_PREFIX + id);
    }
}
