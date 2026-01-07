package com.library.sdl.UserSeat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "*")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;
    // List<SeatFullInfoDTO> getAllSeatsWithFullInfo()


    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<Seat> getSeats() {
        return seatService.getAllSeats();
    }

    @GetMapping("/full-info")
    public List<SeatFullInfoDTO> getAllSeatsWithFullInformation() {
        return seatService.getAllSeatsWithFullInfo();
    }


    @GetMapping("/with-status")
    public List<SeatStatusDTO> getSeatsWithStatus(@RequestParam(name = "shiftNumber", required = true) String shiftNumber) {
        return seatService.getSeatsWithStatus(shiftNumber);
    }
}

