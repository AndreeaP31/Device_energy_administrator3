package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RestController
public class GatewayController {

    @Value("${auth.service.url}")
    private String authService;

    @Value("${user.service.url}")
    private String userService;

    @Value("${device.service.url}")
    private String deviceService;

    @Value("${monitoring.service.url}")
    private String monitoringService;

    private final RestTemplate restTemplate;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/auth/**")
    public ResponseEntity<?> forwardAuth(HttpServletRequest request) throws IOException {
        return forward(request, authService);
    }
    @RequestMapping("/ws-message/**")
    public ResponseEntity<?> proxyWebSocket(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        // Calculăm URL-ul către serviciul de comunicații (port 8091)
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String targetUrl = "http://localhost:8091" + path + (query != null ? "?" + query : "");

        try {
            // Transferăm headerele originale (inclusiv cele de Upgrade pentru WebSocket)
            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames()).forEach(headerName ->
                    headers.add(headerName, request.getHeader(headerName))
            );

            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

            // Trimitem cererea HTTP inițială (handshake)
            return restTemplate.exchange(targetUrl, HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @RequestMapping("/users/**")
    public ResponseEntity<?> forwardUsers(HttpServletRequest request) throws IOException {
        return forward(request, userService);
    }

    @RequestMapping("/device/**")
    public ResponseEntity<?> forwardDevices(HttpServletRequest request) throws IOException {
        return forward(request, deviceService);
    }
    @RequestMapping("/monitoring/**")
    public ResponseEntity<?> forwardMonitoring(HttpServletRequest request) throws IOException {
        return forward(request, monitoringService);
    }

    private ResponseEntity<?> forward(HttpServletRequest request, String baseUrl) throws IOException {
        String path = request.getRequestURI();

        String queryString = request.getQueryString();
        String url = baseUrl + path + (queryString != null ? "?" + queryString : "");

        System.out.println("═══════════════════════════════════════");
        System.out.println("GATEWAY FORWARD");
        System.out.println("Method: " + request.getMethod());
        System.out.println("Original URI: " + request.getRequestURI());
        System.out.println("Target URL: " + url); // Log the full URL to verify
        System.out.println("Base URL: " + baseUrl);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = request.getHeader("Authorization");
        if (auth != null) {
            headers.set("Authorization", auth);
            System.out.println("Authorization: " + auth.substring(0, Math.min(20, auth.length())) + "...");
        } else {
            System.out.println(" No Authorization header");
        }

        HttpEntity<String> entity;

        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            try {
                byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
                String body = new String(bodyBytes, StandardCharsets.UTF_8);

                System.out.println("Request Body: " + body);

                entity = new HttpEntity<>(body, headers);
            } catch (IOException e) {
                System.err.println("Error reading request body:");
                System.err.println("   Exception: " + e.getClass().getName());
                System.err.println("   Message: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } else {
            System.out.println(" No body (GET/DELETE/HEAD)");
            entity = new HttpEntity<>(headers);
        }

        try {
            System.out.println(" Sending request to: " + url);
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            System.out.println("Response received:");
            System.out.println("   Status: " + response.getStatusCode());
            String respBody = response.getBody();
            System.out.println("   Body length: " + (respBody != null ? respBody : "null"));

            System.out.println("═══════════════════════════════════════");

            return ResponseEntity
                    .status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        }catch (HttpStatusCodeException ex) {
            System.err.println(" ERROR FROM MICROSERVICE:");
            System.err.println("   Status: " + ex.getStatusCode());
            System.err.println("   Body: " + ex.getResponseBodyAsString());
            System.err.println("═══════════════════════════════════════");

            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println(" UNEXPECTED GATEWAY ERROR:");
            e.printStackTrace();
            System.err.println("═══════════════════════════════════════");

            return ResponseEntity
                    .status(500)
                    .body("Gateway INTERNAL ERROR: " + e.getMessage());
        }
    }
}