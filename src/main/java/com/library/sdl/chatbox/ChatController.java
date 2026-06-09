package com.library.sdl.chatbox;

import com.library.sdl.idCard.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Tag(name = "SDL AI Chat", description = "Llama-powered assistant for students and admins")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ShastraLibraryChatService chatService;

    public ChatController(ShastraLibraryChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Student chat: JWT with ROLE_USER. Answers about own seat, payments, requests, and general SDL info.
     */
    @Operation(summary = "Student chat", description = "Answers about your seat, payments, requests, and SDL policies")
    @PostMapping("/student")
    public ResponseEntity<ChatMessageResponse> studentChat(@RequestBody ChatMessageRequest request) {
        requireRole("ROLE_USER");
        Long userId = currentStudentId();
        return respond(false, userId, request);
    }

    /**
     * Admin chat: JWT with ROLE_ADMIN. Answers using full library DB summaries.
     */
    @Operation(summary = "Admin chat", description = "Library-wide stats: students, seats, pending requests, overdue payments")
    @PostMapping("/admin")
    public ResponseEntity<ChatMessageResponse> adminChat(@RequestBody ChatMessageRequest request) {
        requireRole("ROLE_ADMIN");
        return respond(true, null, request);
    }

    /**
     * Role-aware endpoint: uses JWT authorities to pick student vs admin context.
     */
    @Operation(summary = "Auto role chat", description = "Uses JWT role to pick student or admin context")
    @PostMapping
    public ResponseEntity<ChatMessageResponse> chat(@RequestBody ChatMessageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        boolean admin = hasRole(auth, "ROLE_ADMIN");
        if (!admin && !hasRole(auth, "ROLE_USER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student or admin role required");
        }
        Long userId = admin ? null : currentStudentId();
        return respond(admin, userId, request);
    }

    @Operation(summary = "Chat health check", security = {})
    @GetMapping("/health")
    public ResponseEntity<ChatMessageResponse> health() {
        return ResponseEntity.ok(new ChatMessageResponse(
                "SDL Llama chat is running. Use POST /api/chat/student or /api/chat/admin with Bearer JWT.",
                "SYSTEM"
        ));
    }

    private ResponseEntity<ChatMessageResponse> respond(boolean admin, Long userId, ChatMessageRequest request) {
        String message = request != null ? request.getMessage() : null;
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ChatMessageResponse("Message cannot be empty", admin ? "ADMIN" : "STUDENT"));
        }

        try {
            logger.info("{} chat: {}", admin ? "Admin" : "Student", message);
            String reply = chatService.chat(admin, userId, message.trim());
            String roleLabel = admin ? "ADMIN" : "STUDENT";
            return ResponseEntity.ok(new ChatMessageResponse(reply, roleLabel));
        } catch (IllegalStateException ex) {
            logger.error("Ollama unavailable", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ChatMessageResponse(ex.getMessage(), admin ? "ADMIN" : "STUDENT"));
        } catch (Exception ex) {
            logger.error("Chat error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatMessageResponse("Error: " + ex.getMessage(), admin ? "ADMIN" : "STUDENT"));
        }
    }

    private static void requireRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !hasRole(auth, role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, role + " required");
        }
    }

    private static boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private static Long currentStudentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student profile not found in token");
    }
}
