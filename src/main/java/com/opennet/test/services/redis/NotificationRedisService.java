package com.opennet.test.services.redis;

import com.opennet.test.model.Notification;
import java.util.List;

public interface NotificationRedisService {
    void addNotificationToRecentCache(Notification notification);
    void cacheRecentNotifications(List<Notification> notifications);
    List<Notification> getRecentNotificationsFromCache();
    void clearRecentNotificationsCache();
    void removeNotificationFromRecentCache(Notification notification);
    Notification getNotificationFromCache(Long id);
    void cacheNotification(Notification notification);
    void evictNotificationFromCache(Long id);
}
