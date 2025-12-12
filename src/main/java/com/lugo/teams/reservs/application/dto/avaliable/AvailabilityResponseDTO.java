// package com.lugo.teams.reservs.application.dto.availability;
package com.lugo.teams.reservs.application.dto.avaliable;

import com.lugo.teams.reservs.application.dto.avaliable.AvailabilitySlotDTO;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponseDTO {
    private Long fieldId;
    private String date; // YYYY-MM-DD (cliente-friendly), también incluimos timezone abajo
    private String timezone;
    private List<AvailabilitySlotDTO> slots;
}
