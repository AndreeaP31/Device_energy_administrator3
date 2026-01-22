package com.example.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {
    private final WebClient webClient;
    private final String apiKey = "CHEIA_TA_API_AICI";

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent").build();
    }

    public String getAiResponse(String prompt) {
        try {
            // Structura JSON cerută de Google Gemini API
            var requestBody = Map.of("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", prompt)
                    ))
            ));

            Map response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Navigare prin JSON-ul de răspuns pentru a extrage textul
            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);

            return (String) firstPart.get("text");
        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }
}