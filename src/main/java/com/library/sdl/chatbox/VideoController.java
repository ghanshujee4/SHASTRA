package com.library.sdl.chatbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Value("${video.api.key}")
    private String apiKey;

    @PostMapping
    public ResponseEntity<Map<String, String>> generateVideo(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt cannot be empty"));
        }
        try {
            Map<String, String> result = callVidfulApi(prompt);
            if (result.containsKey("videoUrl")) {
                return ResponseEntity.ok(Map.of("videoUrl", result.get("videoUrl")));
            } else if (result.containsKey("taskId")) {
                return ResponseEntity.ok(Map.of("taskId", result.get("taskId")));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "No video generated"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    private Map<String, String> callVidfulApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vidful.ai/api/v1/veo/generate"; // Correct endpoint!

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", prompt);
        payload.put("model", "veo3"); // Confirm the actual model name via docs
        payload.put("aspectRatio", "16:9"); // Optional

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        Map<String, String> result = new HashMap<>();

        // Parse response (adjust as per real API spec)
        if (body != null) {
            // If you get video URL directly:
            if (body.containsKey("videoUrl")) {
                result.put("videoUrl", body.get("videoUrl").toString());
            }
            // Or if you get a task and need to poll for status:
            else if (body.containsKey("taskId")) {
                result.put("taskId", body.get("taskId").toString());
            }
            // Add more key mappings if needed from official response
        }
        return result;
    }
}
