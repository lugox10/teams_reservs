package com.lugo.teams.reservs.application.dto.reserv;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {

    private String userName;
    private Long userId;
    private Long venueId;

    @NotNull
    private Long fieldId;

    private Long timeSlotId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // nueva duración (minutos)
    @Min(1)
    @Max(60)
    private Integer durationMinutes;

    @Min(1)
    private Integer playersCount = 1;

    private String teamName;
    private String notes;

    private boolean createTeamsMatch = false;

    // guest fields (si reserva como invitado)
    private String guestName;
    private String guestPhone;
    private String guestEmail;
}
