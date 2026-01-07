
package com.library.sdl.idCard;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.payment.PaymentRecord;
import com.library.sdl.payment.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idcard")
@RequiredArgsConstructor
public class IdCardController {

    private final PaymentRecordService paymentRecordService;
    private final IdCardService idCardService;
    private final UserRepository userRepo;

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadIdCard(
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepo.findById(principal.getId())
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
            @AuthenticationPrincipal CustomUserDetails principal) {

        User user = userRepo.findById(principal.getId())
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


}
