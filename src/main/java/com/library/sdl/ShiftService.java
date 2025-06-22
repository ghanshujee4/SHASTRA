package com.library.sdl;

// import com.example.demo.model.Shift;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    /**
     * Fetch all shifts.
     * Replace with actual database fetching logic if necessary.
     */
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
}
