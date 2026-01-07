package com.library.sdl.idCard;

import java.time.LocalDate;

public record IdCardDTO(
        String name,
        Long mobile,
        String seat,
        String shift,
        LocalDate validTill
) {}
