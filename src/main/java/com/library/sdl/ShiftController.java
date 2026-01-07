package com.library.sdl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/shifts") // Base endpoint for Shift APIs
@CrossOrigin(origins = "*")
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * Get a list of all shifts
     */
    @GetMapping
    public List<Shift> getShifts() {
        return shiftService.getAllShifts();
    }
}
