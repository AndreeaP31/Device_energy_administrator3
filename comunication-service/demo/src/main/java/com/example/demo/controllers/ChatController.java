package com.example.demo.controllers;

import com.example.demo.dtos.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendToAdmin")
    public void sendMessageToAdmin(@Payload ChatMessage chatMessage) {
        // Admin-ul va fi abonat la /topic/admin-messages pentru a vedea toate cererile de suport
        messagingTemplate.convertAndSend("/topic/admin-messages", chatMessage);
    }
    @MessageMapping("/chat.replyToClient")
    public void replyToClient(@Payload ChatMessage chatMessage) {
        // Trimite mesajul direct către coada privată a clientului
        // Clientul trebuie să fie abonat la /user/queue/private pe frontend
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverName(), "/queue/private", chatMessage);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage chatMessage) {
        // Redirecționăm notificarea de "scrie cineva..." către celălalt user
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverName(), "/queue/typing", chatMessage);
   }
//    @MessageMapping("/chat.typing")
//    public void sendTypingNotification(@Payload ChatMessage chatMessage) {
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getReceiverName(), "/queue/typing", chatMessage);
//    }
    // Handlers for sending messages
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        if ("admin".equalsIgnoreCase(chatMessage.getReceiverName())) {
            messagingTemplate.convertAndSend("/topic/admin-messages", chatMessage);
        } else {
            // Dacă adminul răspunde, trimitem către coada privată a clientului
            // Frontend-ul clientului trebuie să fie abonat la /user/queue/private
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverName(), "/queue/private", chatMessage);
        }
    }

    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        // Sends message to /user/{username}/queue/private
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverName(), "/queue/private", chatMessage);
    }
}
