package com.example.demo;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        System.out.println("JwtAuthenticationFilter - Processing: " + req.getMethod() + " " + req.getRequestURI());


        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("  OPTIONS preflight → bypass authentication");
            HttpServletResponse resp = (HttpServletResponse) response;

            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String fullPath = req.getRequestURI();
        if (fullPath.contains("/ws-message")) {
            System.out.println("   WebSocket detected → bypass JWT filter for handshake");
            chain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(req);

        String path = cachedRequest.getRequestURI();

        if (path.startsWith("/api")) {
            path = path.substring(4);
        }


        System.out.println("   Path after prefix removal: " + path);

        if (path.startsWith("/auth")) {
            System.out.println("   Public auth endpoint - allowing without token");
            chain.doFilter(cachedRequest, response);
            return;
        }

        if (path.equals("/users") && cachedRequest.getMethod().equals("POST")) {
            System.out.println("   POST /users - allowing without token (internal call)");
            chain.doFilter(cachedRequest, response);
            return;
        }
        if (path.startsWith("/ws-message")) {
            System.out.println("   WebSocket handshake - allowing for STOMP connection");
            chain.doFilter(cachedRequest, response);
            return;
        }

        String authHeader = cachedRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("   Missing or invalid Authorization header");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("   Token found, validating...");

        try {
            URL url = new URL(authServiceUrl + "/auth/validate");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                System.err.println("   Token validation failed with status: " + responseCode);
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(con.getInputStream(), Map.class);

            String role = (String) data.get("role");
            Object userId = data.get("userId");

            cachedRequest.setAttribute("role", role);
            cachedRequest.setAttribute("userId", userId);

            System.out.println("   Token valid - Role: " + role + ", UserId: " + userId);

            chain.doFilter(cachedRequest, response);

        } catch (Exception e) {
            System.err.println("   Error validating token: " + e.getMessage());
            e.printStackTrace();
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation error");
        }
    }
}
