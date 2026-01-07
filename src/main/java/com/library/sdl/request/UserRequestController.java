package com.library.sdl.request;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class UserRequestController {

    @Autowired
    private UserRequestService userRequestService;

    @PostMapping("/{userId}")
    public ResponseEntity<UserRequest> createRequest(
            @PathVariable Long userId,
            @RequestParam("type") RequestType type,
            @RequestParam(value = "details", required = false) String details) {
        UserRequest newReq = userRequestService.createRequest(userId, type, details);
        return ResponseEntity.ok(newReq);
    }

    @GetMapping
    public List<UserRequest> getAllRequests() {
        return userRequestService.getAllRequests();
    }

    @GetMapping("/{userId}")
    public List<UserRequest> getUserRequests(@PathVariable Long userId) {
        return userRequestService.getUserRequests(userId);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<UserRequest> approveRequest(@PathVariable Long id) throws MessagingException {
        return ResponseEntity.ok(userRequestService.approveRequest(id));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<UserRequest> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(userRequestService.rejectRequest(id));
    }

    @PostMapping("/activate/{userId}")
    public ResponseEntity<UserRequest> requestActivation(@PathVariable Long userId) {
        UserRequest req = userRequestService.createRequest(userId, RequestType.ACTIVATION, "New user activation request");
        return ResponseEntity.ok(req);
    }

}
