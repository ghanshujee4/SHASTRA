
package com.library.sdl.UserSeat;

import com.library.sdl.User;
import com.library.sdl.UserRepository;
import com.library.sdl.payment.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatService.class);

    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository,
                       UserRepository userRepository,
                       PaymentRecordRepository paymentRecordRepository) {
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.paymentRecordRepository = paymentRecordRepository;
    }

    public List<Seat> getAllSeats() {
        logger.debug("Fetching all seats from repository");
        return seatRepository.findAll();
    }

    public List<SeatStatusDTO> getSeatsWithStatus(String shiftNumber) {
        logger.info("Fetching seat status for shift(s): {}", shiftNumber);

        List<String> shifts = Arrays.stream(shiftNumber.split(","))
                .map(String::trim)
                .toList();

        List<Seat> allSeats = getAllSeats();

        Set<String> registeredSeats = shifts.stream()
                .flatMap(shift -> {
                    // Assuming findRegisteredSeatsByShift returns List<String> of "seatNumber,name"
                    List<String> foundSeats = userRepository.findRegisteredSeatsByShift(shift);
                    logger.debug("Shift: {}, Registered Seats (raw): {}", shift, foundSeats);

                    return foundSeats.stream()
                            .map(seatStr -> seatStr.split(",")[0].trim()); // take only seat number
                })
                .collect(Collectors.toSet());

        logger.info("Final Registered Seats Set: {}", registeredSeats);

        return allSeats.stream()
                .map(seat -> {
                    boolean isRegistered = registeredSeats.contains(String.valueOf(seat.getSeatNo()));
                    logger.debug("Seat: {}, Registered: {}", seat.getSeatNo(), isRegistered);
                    return new SeatStatusDTO(seat.getSeatNo(), isRegistered);
                })
                .collect(Collectors.toList());
    }

    public List<PaymentRecord> getUserPayments(Long userId) {
        logger.info("Fetching payment records for user ID: {}", userId);
        return paymentRecordRepository.findByUserId(userId);
    }

public List<SeatFullInfoDTO> getAllSeatsWithFullInfo() {
    logger.info("Fetching full seat info (active users with multiple shifts)");

    // Step 1: Get active users
    List<User> allUsers = userRepository.findAll().stream()
            .filter(user -> "Y".equalsIgnoreCase(user.getIsRegistered()))
            .filter(user -> user.getSeat() != null) // Must be assigned a seat
            .toList();

    // Step 2: Get seats for lookup
    Map<String, Seat> seatMap = seatRepository.findAll().stream()
            .collect(Collectors.toMap(
                    seat -> String.valueOf(seat.getSeatNo()),
                    seat -> seat
            ));

    // Step 3: Get latest payment per user
    Map<Long, PaymentRecord> userToLatestPaymentMap = paymentRecordRepository.findAll().stream()
            .filter(p -> p.getUser() != null)
            .collect(Collectors.groupingBy(
                    p -> p.getUser().getId(),
                    Collectors.collectingAndThen(
                            Collectors.maxBy(Comparator.comparing(PaymentRecord::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder()))),
                            opt -> opt.orElse(null)
                    )
            ));

    logger.debug("Built user-to-latest-payment map of size: {}", userToLatestPaymentMap.size());

    // Step 4: Build DTOs per user-shift-seat combo
    return allUsers.stream()
            .map(user -> {
                String extraHour =
                        (user.getExtraHour() == null || user.getExtraHour().isBlank())
                                ? "0"
                                : user.getExtraHour();

                Seat seat = seatMap.get(user.getSeat());
                PaymentRecord payment = userToLatestPaymentMap.get(user.getId());

                return new SeatFullInfoDTO(
                        seat != null ? seat.getSeatNo() : null,
                        user.getId(),
                        user.getName(),
                        user.getMobile(),
                        user.getShift(),
                        payment != null ? payment.getPaid() : null,
                        payment != null ? payment.getDueDate() : null,
                        // user.getExtraHour()
                        extraHour
                );
            })
            .collect(Collectors.toList());
}
}
