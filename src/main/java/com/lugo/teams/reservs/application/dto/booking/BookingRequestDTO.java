package com.lugo.teams.reservs.application.dto.booking;

import lombok.Data;

@Data
public class BookingRequestDTO {
    private Long scheduleId;
    private Long userId; // Por ahora interno (Teams FC monolito)
}
