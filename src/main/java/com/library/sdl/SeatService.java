//package com.library.sdl;
//
//import com.library.sdl.UserSeat.SeatFullInfoDTO;
//import com.library.sdl.payment.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Service
//public class SeatService {
//    @Autowired
//    private final SeatRepository seatRepository;
//    @Autowired
//    private final UserRepository userRepository;
//    @Autowired
//    private PaymentRecordRepository paymentRecordRepository;
//    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
//
//    public SeatService(SeatRepository seatRepository, UserRepository userRepository) {
//        this.seatRepository = seatRepository;
//        this.userRepository = userRepository;
//    }
//
//    public List<Seat> getAllSeats() {
//        // Fetch all seats, assuming seatRepository has the required method
//        return seatRepository.findAll();
//    }
//
//    public List<SeatStatusDTO> getSeatsWithStatus(String shiftNumber) {
//        Logger logger = LoggerFactory.getLogger(this.getClass());
//
//        // Convert the comma-separated shift numbers into a List
//        List<String> shifts = Arrays.stream(shiftNumber.split(","))
//                .map(String::trim)
//                .collect(Collectors.toList());
//
//        // Fetch all seats
//        List<Seat> allSeats = getAllSeats();
//
//        // Fetch registered seats and convert to String for consistency
//        Set<String> registeredSeats = shifts.stream()
//                .flatMap(shift -> {
//                    List<String> foundSeats = userRepository.findRegisteredSeatsByShift(shift);
//                    logger.info("Shift: {}, Registered Seats: {}", shift, foundSeats);
//                    return foundSeats.stream().map(String::valueOf); // Convert to String
//                })
//                .collect(Collectors.toSet());
//
//        logger.info("Final Registered Seats: {}", registeredSeats);
//
//        // Map all seats with their status (true if registered, false otherwise)
//        return allSeats.stream()
//                .map(seat -> {
//                    boolean isRegistered = registeredSeats.contains(String.valueOf(seat.getSeatNo())); // Convert to String
//                    logger.info("Seat: {}, Is Registered: {}", seat.getSeatNo(), isRegistered);
//                    return new SeatStatusDTO(seat.getSeatNo(), isRegistered);
//                })
//                .collect(Collectors.toList());
//    }
//    public List<PaymentRecord> getUserPayments(Long userId) {
//        return paymentRecordRepository.findByUserId(userId);
//    }
//
//    public List<SeatFullInfoDTO> getAllSeatsWithFullInfo() {
//        List<Seat> allSeats = seatRepository.findAll(); // Your Seat JPA repo
//        List<User> allUsers = userRepository.findAll();
//        List<PaymentRecord> allPayments = paymentRecordRepository.findAll();
//
//        // Map seat number (String) to User
//        Map<String, List<User>> seatToUserMap = allUsers.stream()
//                .filter(user -> user.getSeat() != null)
//                .collect(Collectors.groupingBy(User::getSeat));
//
//
//        // Map user IDto Payment (latest)
//        // Get latest payment per user by paymentDate
//        Map<Long, PaymentRecord> userToLatestPaymentMap = allPayments.stream()
//                .filter(p -> p.getUser() != null)
//                .collect(Collectors.groupingBy(
//                        p -> p.getUser().getId(),
//                        Collectors.collectingAndThen(
//                                Collectors.maxBy(Comparator.comparing(PaymentRecord::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder()))),
//                                opt -> opt.orElse(null)
//                        )
//                ));
//
//        return allSeats.stream()
//                .flatMap(seat -> {
//                    List<User> users = seatToUserMap.getOrDefault(String.valueOf(seat.getSeatNo()), Collections.emptyList());
//
//                    if (!users.isEmpty()) {
//                        return users.stream().map(user -> {
//                            PaymentRecord payment = userToLatestPaymentMap.get(user.getId());
//
//                            return new SeatFullInfoDTO(
//                                    seat.getSeatNo(),
//                                    user.getId(),
//                                    user.getName(),
//                                    user.getEmail(),
//                                    user.getMobile(),
//                                    user.getShift(),
//                                    user.getAddress(),
//                                    payment != null ? payment.getPaid() : null,
//                                    payment != null ? payment.getDueDate() : null,
//                                    payment != null ? payment.getPaymentDate() : null,
//                                    payment != null ? payment.getAmount() : null,
//                                    payment != null ? payment.getComments() : null
//                            );
//                        });
//                    } else {
//                        return Stream.of(new SeatFullInfoDTO(
//                                seat.getSeatNo(),
//                                null, null, null, null, null, null,
//                                null, null, null, null, null
//                        ));
//                    }
//                })
//                .collect(Collectors.toList());
//    }
//
//}
//
//
//
package com.library.sdl;

import com.library.sdl.UserSeat.SeatFullInfoDTO;
import com.library.sdl.payment.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .collect(Collectors.toList());

        List<Seat> allSeats = getAllSeats();

        Set<String> registeredSeats = shifts.stream()
                .flatMap(shift -> {
                    List<String> foundSeats = userRepository.findRegisteredSeatsByShift(shift);
                    logger.debug("Shift: {}, Registered Seats: {}", shift, foundSeats);
                    return foundSeats.stream().map(String::valueOf);
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
        logger.info("Fetching full seat info (with users and payments)");

        List<Seat> allSeats = seatRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<PaymentRecord> allPayments = paymentRecordRepository.findAll();

        Map<String, List<User>> seatToUserMap = allUsers.stream()
                .filter(user -> user.getSeat() != null)
                .collect(Collectors.groupingBy(User::getSeat));

        Map<Long, PaymentRecord> userToLatestPaymentMap = allPayments.stream()
                .filter(p -> p.getUser() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(PaymentRecord::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder()))),
                                opt -> opt.orElse(null)
                        )
                ));

        logger.debug("Built user-to-latest-payment map of size: {}", userToLatestPaymentMap.size());

        return allSeats.stream()
                .flatMap(seat -> {
                    List<User> users = seatToUserMap.getOrDefault(String.valueOf(seat.getSeatNo()), Collections.emptyList());

                    if (!users.isEmpty()) {
                        return users.stream().map(user -> {
                            PaymentRecord payment = userToLatestPaymentMap.get(user.getId());

                            return new SeatFullInfoDTO(
                                    seat.getSeatNo(),
                                    user.getId(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getMobile(),
                                    user.getShift(),
                                    user.getAddress(),
                                    payment != null ? payment.getPaid() : null,
                                    payment != null ? payment.getDueDate() : null,
                                    payment != null ? payment.getPaymentDate() : null,
                                    payment != null ? payment.getAmount() : null,
                                    payment != null ? payment.getComments() : null
                            );
                        });
                    } else {
                        return Stream.of(new SeatFullInfoDTO(
                                seat.getSeatNo(),
                                null, null, null, null, null, null,
                                null, null, null, null, null
                        ));
                    }
                })
                .collect(Collectors.toList());
    }
}
