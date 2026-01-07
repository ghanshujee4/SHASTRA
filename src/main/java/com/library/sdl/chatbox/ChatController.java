package com.library.sdl.chatbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${google.api.key}")
    private String apiKey;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("reply", "Message cannot be empty"));
            }

            logger.info("Chat request: {}", message);

            // Call Google Gemini API directly
            String reply = callGeminiApi(message);

            logger.info("Chat response: {}", reply);

            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            logger.error("Chat error", e);
            return ResponseEntity.status(500)
                    .body(Map.of("reply", "Error: " + e.getMessage()));
        }
    }

    private String callGeminiApi(String message) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new java.util.ArrayList<>();

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");

        List<Map<String, String>> parts = new java.util.ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", message);
        parts.add(part);

        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        // Call API
        try {
            Map response = restTemplate.postForObject(url, requestBody, Map.class);

            // Extract text from response
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                List<Map<String, String>> partsResponse = (List<Map<String, String>>) contentResponse.get("parts");
                if (partsResponse != null && !partsResponse.isEmpty()) {
                    return partsResponse.getFirst().get("text");
                }
            }
            return "No response from API";
        } catch (Exception e) {
            throw new RuntimeException("API Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Chat API is running"));
    }
}
