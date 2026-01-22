package com.example.demo.controllers;

import com.example.demo.dtos.ChatMessage;
import com.example.demo.services.GeminiService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> botRules = new HashMap<>();
    private final GeminiService geminiService;
    public ChatController(SimpMessagingTemplate messagingTemplate, GeminiService geminiService) {
        this.messagingTemplate = messagingTemplate;
        this.geminiService = geminiService;
        initializeRules();
    }

    private void initializeRules() {
        botRules.put("salut", "Bună! Cum te pot ajuta astăzi cu sistemul tău de energie?");
        botRules.put("ajutor", "Sunt aici să te ajut! Poți întreba despre 'consum', 'dispozitive' sau 'alerte'.");
        botRules.put("consum", "Poți vedea consumul orar în secțiunea Dashboard selectând un dispozitiv și o dată.");
        botRules.put("Cum adaug un dispozitiv", "Pentru a adăuga un dispozitiv nou, contactează administratorul la admin@energy.com.");
        botRules.put("Ce e o alerta alerta", "Alertele apar automat când un dispozitiv depășește limita setată de consum orar.");
        botRules.put("Ce e limita", "Limita de consum este stabilită de administrator pentru fiecare dispozitiv în parte.");
        botRules.put("eroare", "Dacă întâmpini o eroare de conexiune, verifică dacă ești logat corect.");
        botRules.put("Vreau  sa modific informatii cont", "Informațiile despre contul tău pot fi modificate de către un Administrator.");
        botRules.put("grafic", "Graficul afișează consumul total per oră. Asigură-te că simulatorul trimite date.");
        botRules.put("multumesc", "Cu plăcere! O zi plină de energie!");
    }

    @MessageMapping("/chat.send1")
    public void sendMessage1(@Payload ChatMessage chatMessage) {
        // 1. Trimitem mesajul original (de la user la admin/chat)
        messagingTemplate.convertAndSend("/topic/public", chatMessage);

        // 2. Logica Chatbot-ului (Rule-based)
        String content = chatMessage.getContent().toLowerCase();
        String botResponse = null;

        for (String keyword : botRules.keySet()) {
            if (content.contains(keyword)) {
                botResponse = botRules.get(keyword);
                break;
            }
        }

        if (botResponse != null) {
            ChatMessage botMsg = new ChatMessage();
            botMsg.setSenderName("EnergyBot");
            botMsg.setContent(botResponse);
            botMsg.setTimestamp(LocalDateTime.now().toString());

            // Trimitem răspunsul botului înapoi pe canalul public
            messagingTemplate.convertAndSend("/topic/public", botMsg);
        }
    }

//    public ChatController(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    @MessageMapping("/chat.sendToAdmin")
//    public void sendMessageToAdmin(@Payload ChatMessage chatMessage) {
//        // Admin-ul va fi abonat la /topic/admin-messages pentru a vedea toate cererile de suport
//        messagingTemplate.convertAndSend("/topic/admin-messages", chatMessage);
//    }
//    @MessageMapping("/chat.replyToClient")
//    public void replyToClient(@Payload ChatMessage chatMessage) {
//        // Trimite mesajul direct către coada privată a clientului
//        // Clientul trebuie să fie abonat la /user/queue/private pe frontend
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getReceiverName(), "/queue/private", chatMessage);
//    }
//
//    @MessageMapping("/chat.typing")
//    public void handleTyping(@Payload ChatMessage chatMessage) {
//        // Redirecționăm notificarea de "scrie cineva..." către celălalt user
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getReceiverName(), "/queue/typing", chatMessage);
   //}
//    @MessageMapping("/chat.typing")
//    public void sendTypingNotification(@Payload ChatMessage chatMessage) {
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.getReceiverName(), "/queue/typing", chatMessage);
//    }
    // Handlers for sending messages
//    @MessageMapping("/chat.sendMessage")
//    public void sendMessage(@Payload ChatMessage chatMessage) {
//        if ("admin".equalsIgnoreCase(chatMessage.getReceiverName())) {
//            messagingTemplate.convertAndSend("/topic/admin-messages", chatMessage);
//        } else {
//            // Dacă adminul răspunde, trimitem către coada privată a clientului
//            // Frontend-ul clientului trebuie să fie abonat la /user/queue/private
//            messagingTemplate.convertAndSendToUser(
//                    chatMessage.getReceiverName(), "/queue/private", chatMessage);
//        }
//    }
@MessageMapping("/chat.send")
public void sendMessage(@Payload ChatMessage chatMessage) {
    // 1. Trimitem mesajul original pe canalul public (pentru a apărea în UI-ul utilizatorului)
    messagingTemplate.convertAndSend("/topic/public", chatMessage);

    String content = chatMessage.getContent().toLowerCase();
    if (content.contains("suport ai") || content.contains("ai suport")) {
        // Notificăm utilizatorul că AI-ul procesează
        sendBotMessage("Sistemul AI se gândește...");

        // Apelăm serviciul Gemini
        String aiResponse = geminiService.getAiResponse(chatMessage.getContent());

        // Trimitem răspunsul generat de AI
        sendBotMessage(aiResponse);
        return; // Oprim execuția pentru a nu activa chatbot-ul de reguli
    }

    // 2. Logica de rutare către ADMIN
    if (content.contains("ajutor")) {
        // Trimitem o notificare specială către admin pe un topic dedicat
        messagingTemplate.convertAndSend("/topic/admin-message", chatMessage);

        // Opțional: Confirmăm utilizatorului că un admin a fost notificat
        ChatMessage notification = new ChatMessage();
        notification.setSenderName("EnergyBot");
        notification.setContent("Mesajul tău a fost trimis către un administrator. Vei primi un răspuns în curând!");
        notification.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/public", notification);
        return; // Oprim execuția aici pentru a nu mai trece prin regulile de bot de mai jos
    }

    // 3. Logica de Chatbot existentă (Rule-based)
    String botResponse = null;
    for (String keyword : botRules.keySet()) {
        if (content.contains(keyword)) {
            botResponse = botRules.get(keyword);
            break;
        }
    }

    if (botResponse != null) {
        ChatMessage botMsg = new ChatMessage();
        botMsg.setSenderName("EnergyBot");
        botMsg.setContent(botResponse);
        botMsg.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/public", botMsg);
    }
}

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        String destination = "/topic/chat/" + chatMessage.getReceiverId();
        messagingTemplate.convertAndSend(destination, chatMessage);

        messagingTemplate.convertAndSend("/topic/admin/messages", chatMessage);
    }
}
