package com.lugo.teams.reservs.application.dto.booking;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {
    private Long id;
    private Long scheduleId;
    private Long userId;
    private LocalDateTime fechaReserva;
    private String estado; // RESERVADO / CANCELADO
}
