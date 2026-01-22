
package com.opennet.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opennet.test.model.Notification;
import com.opennet.test.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("null")
    @Test
    public void testCreateNotification() throws Exception {
        Notification notification = new Notification();
        notification.setSubject("Test Subject");
        notification.setContent("Test Content");
        notification.setCreatedAt(ZonedDateTime.now());

        when(notificationService.createNotification(any(Notification.class))).thenReturn(notification);

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    public void testGetNotificationById() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setSubject("Test Subject");

        when(notificationService.getNotificationById(1L)).thenReturn(notification);

        mockMvc.perform(get("/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    public void testGetRecentNotifications() throws Exception {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setSubject("Subject 1");
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setSubject("Subject 2");
        List<Notification> notifications = Arrays.asList(notification1, notification2);

        when(notificationService.getRecentNotifications(2)).thenReturn(notifications);

        mockMvc.perform(get("/notifications/recent").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @SuppressWarnings("null")
    @Test
    public void testUpdateNotification() throws Exception {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setSubject("Updated Subject");

        when(notificationService.updateNotification(anyLong(), any(Notification.class))).thenReturn(notification);

        mockMvc.perform(put("/notifications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subject").value("Updated Subject"));
    }

    @Test
    public void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isOk());
    }
}
