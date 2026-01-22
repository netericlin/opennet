
package com.opennet.test.services;

import com.opennet.test.model.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    Notification getNotificationById(Long id);
    List<Notification> getRecentNotifications(int limit);
    Notification updateNotification(Long id, Notification updatedNotification);
    void deleteNotification(Long id);
}
