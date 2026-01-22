
package com.opennet.test.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opennet.test.exception.MessagingException;
import com.opennet.test.exception.ResourceNotFoundException;
import com.opennet.test.model.Notification;
import com.opennet.test.repository.NotificationRepository;
import com.opennet.test.services.messaging.RocketMQServiceImpl;
import com.opennet.test.services.redis.NotificationRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRedisService notificationRedisService;

    @Mock
    private RocketMQServiceImpl rocketMQService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
    }

    @SuppressWarnings("null")
    @Test
    void testCreateNotification() throws Exception {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        Notification created = notificationService.createNotification(new Notification());

        assertNotNull(created);
        assertEquals(notification.getId(), created.getId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(rocketMQService, times(1)).sendMessage(anyString(), anyString());
        verify(notificationRedisService, times(1)).addNotificationToRecentCache(any(Notification.class));
    }

    @SuppressWarnings("null")
    @Test
    void testCreateNotification_MessagingException() throws Exception {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new RuntimeException("Error sending message")).when(rocketMQService).sendMessage(anyString(),
                anyString());

        assertThrows(MessagingException.class, () -> {
            notificationService.createNotification(new Notification());
        });

        verify(notificationRedisService, times(1)).addNotificationToRecentCache(any(Notification.class));
        verify(notificationRedisService, times(1)).removeNotificationFromRecentCache(any(Notification.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(rocketMQService, times(1)).sendMessage(anyString(), anyString());
    }

    @Test
    void testGetNotificationById_FoundInCache() {
        when(notificationRedisService.getNotificationFromCache(1L)).thenReturn(notification);

        Notification found = notificationService.getNotificationById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(notificationRedisService, times(1)).getNotificationFromCache(1L);
        verify(notificationRepository, never()).findById(1L);
    }

    @Test
    void testGetNotificationById_FoundInDb() {
        when(notificationRedisService.getNotificationFromCache(1L)).thenReturn(null);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification found = notificationService.getNotificationById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(notificationRedisService, times(1)).getNotificationFromCache(1L);
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRedisService, times(1)).cacheNotification(notification);
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRedisService.getNotificationFromCache(1L)).thenReturn(null);
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(1L);
        });
    }

    @SuppressWarnings("null")
    @Test
    void testUpdateNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification updatedNotification = new Notification();
        updatedNotification.setSubject("Updated Subject");

        updatedNotification = notificationService.updateNotification(1L, updatedNotification);

        assertNotNull(updatedNotification);
        assertEquals("Updated Subject", updatedNotification.getSubject());
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(notificationRedisService, times(1)).removeNotificationFromRecentCache(any(Notification.class));
        verify(notificationRedisService, times(1)).cacheNotification(any(Notification.class));
        verify(notificationRedisService, times(1)).addNotificationToRecentCache(any(Notification.class));
    }

    @Test
    void testUpdateNotification_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.updateNotification(1L, new Notification());
        });
    }
}
