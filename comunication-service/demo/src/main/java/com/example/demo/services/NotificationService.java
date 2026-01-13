package com.example.demo.services;

import com.example.demo.dtos.AlertMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "overconsumption_queue")
    public void handleOverconsumption(AlertMessage alert) {
        System.out.println("Trimitere alertă prin WebSocket către user: " + alert.getUserId());

        // Trimitem mesajul către topicul la care React va fi abonat
        // Exemplu topic: /topic/notifications/123-uuid
        messagingTemplate.convertAndSend("/topic/notifications/" + alert.getUserId(), alert);
    }
}