package com.lugo.teams.reservs.application.dto.reserv;

import com.lugo.teams.reservs.domain.model.PaymentStatus;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {

    private Long id;
    private String userName;
    private Long userId;

    private Long fieldId;
    private String fieldName;
    private Long venueId;
    private String venueName;

    private Long timeSlotId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    /**
     * Duración calculada (minutos) = end - start (útil para la vista).
     */
    private Integer durationMinutes;

    private Integer playersCount;
    private String teamName;

    private ReservationStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal totalAmount;
    private String paymentReference;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReservationTeamLinkDTO> links;
}
