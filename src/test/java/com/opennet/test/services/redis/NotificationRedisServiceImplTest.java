
package com.opennet.test.services.redis;

import com.opennet.test.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationRedisServiceImplTest {

    private static final String RECENT_NOTIFICATIONS_KEY = "recent_notifications";
    private static final String NOTIFICATION_KEY_PREFIX = "notification:";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private NotificationRedisServiceImpl notificationRedisService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setSubject("Test Subject");
        notification.setContent("Test Content");
    }

    @SuppressWarnings("null")
    @Test
    void testAddNotificationToRecentCache() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        notificationRedisService.addNotificationToRecentCache(notification);

        verify(listOperations, times(1)).leftPush(RECENT_NOTIFICATIONS_KEY, notification);
        verify(listOperations, times(1)).trim(RECENT_NOTIFICATIONS_KEY, 0, 9);
    }

    @SuppressWarnings("null")
    @Test
    void testCacheRecentNotifications() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        List<Notification> notifications = Arrays.asList(notification, new Notification());
        notificationRedisService.cacheRecentNotifications(notifications);

        verify(redisTemplate, times(1)).delete(RECENT_NOTIFICATIONS_KEY);
        verify(listOperations, times(1)).rightPushAll(RECENT_NOTIFICATIONS_KEY, notifications.toArray());
    }

    @Test
    void testGetRecentNotificationsFromCache() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        List<Object> notifications = Arrays.asList(notification, new Notification());
        when(listOperations.range(RECENT_NOTIFICATIONS_KEY, 0, -1)).thenReturn(notifications);

        List<Notification> result = notificationRedisService.getRecentNotificationsFromCache();

        assertEquals(2, result.size());
        verify(listOperations, times(1)).range(RECENT_NOTIFICATIONS_KEY, 0, -1);
    }

    @Test
    void testClearRecentNotificationsCache() {
        notificationRedisService.clearRecentNotificationsCache();
        verify(redisTemplate, times(1)).delete(RECENT_NOTIFICATIONS_KEY);
    }

    @SuppressWarnings("null")
    @Test
    void testRemoveNotificationFromRecentCache() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        Notification otherNotification = new Notification();
        otherNotification.setId(2L);
        List<Object> notifications = new java.util.ArrayList<>(Arrays.asList(notification, otherNotification));
        when(listOperations.range(RECENT_NOTIFICATIONS_KEY, 0, -1)).thenReturn(notifications);

        notificationRedisService.removeNotificationFromRecentCache(notification);

        verify(redisTemplate, times(1)).delete(RECENT_NOTIFICATIONS_KEY);
        verify(listOperations, times(1)).rightPushAll(eq(RECENT_NOTIFICATIONS_KEY), any(Object[].class));
    }

    @Test
    void testGetNotificationFromCache() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String key = NOTIFICATION_KEY_PREFIX + 1L;
        when(valueOperations.get(key)).thenReturn(notification);

        Notification result = notificationRedisService.getNotificationFromCache(1L);

        assertEquals(notification, result);
        verify(valueOperations, times(1)).get(key);
    }

    @SuppressWarnings("null")
    @Test
    void testCacheNotification() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String key = NOTIFICATION_KEY_PREFIX + notification.getId();
        notificationRedisService.cacheNotification(notification);
        verify(valueOperations, times(1)).set(key, notification, 1, TimeUnit.HOURS);
    }

    @Test
    void testEvictNotificationFromCache() {
        String key = NOTIFICATION_KEY_PREFIX + 1L;
        notificationRedisService.evictNotificationFromCache(1L);
        verify(redisTemplate, times(1)).delete(key);
    }
}
