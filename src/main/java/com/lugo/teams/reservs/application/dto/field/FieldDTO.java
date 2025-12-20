package com.lugo.teams.reservs.application.dto.field;
import com.lugo.teams.reservs.domain.model.FieldType;
import com.lugo.teams.reservs.domain.model.SurfaceType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de salida (response). No tiene validaciones porque se usa para enviar datos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDTO {
    private Long id;
    private Long venueId;
    private String name;
    private FieldType fieldType;
    private SurfaceType surface;
    private Integer capacityPlayers;
    private BigDecimal pricePerHour;
    private Boolean active;
    private List<String> photos;
    private BigDecimal lat;
    private BigDecimal lng;

    // Nuevos campos consistentes con la entidad
    private Integer slotMinutes;
    private Integer openHour;
    private Integer closeHour;
    private Integer minBookingHours;

    /**
     * No exponemos la lista completa de timeSlots por seguridad / simplicidad.
     * Solo devolvemos el conteo; si en el futuro quieres la lista, crear TimeSlotDTO.
     */
    private Integer timeSlotsCount;
}
