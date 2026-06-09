
package com.library.sdl.idCard;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.payment.PaymentRecord;
import com.library.sdl.payment.PaymentRecordService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idcard")
public class IdCardController {

    private final PaymentRecordService paymentRecordService;
    private final IdCardService idCardService;
    private final UserRepository userRepo;
    public IdCardController(
            PaymentRecordService paymentRecordService,
            IdCardService idCardService,
            UserRepository userRepo) {

        this.paymentRecordService = paymentRecordService;
        this.idCardService = idCardService;
        this.userRepo = userRepo;
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadIdCard(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long userId
    ) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // If an admin requests and provides a userId, allow fetching that user's id card.
        Long effectiveUserId = principal.getId();
        if (userId != null) {
            if (isAdmin(principal)) {
                effectiveUserId = userId;
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        User user = userRepo.findById(effectiveUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ BUSINESS RULE
        if (!"Y".equalsIgnoreCase(user.getIsRegistered())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        PaymentRecord payment =
                paymentRecordService.getLatestPaidPayment(user.getId());

        byte[] pdf = idCardService.generateIdCard(
                user,
                payment.getDueDate()
        );
// ✅ SAFETY CHECK
        if (pdf == null || pdf.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=SDL_ID_CARD.pdf")
                .body(pdf);
    }

    @GetMapping("/card-data")
    public IdCardDTO getIdCardData(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long userId
    ) {

        Long effectiveUserId = principal.getId();
        if (userId != null) {
            if (isAdmin(principal)) {
                effectiveUserId = userId;
            } else {
                throw new RuntimeException("Forbidden");
            }
        }

        User user = userRepo.findById(effectiveUserId)
                .orElseThrow();

        PaymentRecord payment =
                paymentRecordService.getLatestPaidPayment(user.getId());

        return new IdCardDTO(
                user.getName(),
                user.getMobile(),
                user.getSeat(),
                user.getShift(),
                payment.getDueDate()   // ✅ single truth
        );
    }

    // Simple helper to check for ADMIN role
    private boolean isAdmin(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }


}
