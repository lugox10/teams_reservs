// package com.lugo.teams.reservs.application.dto.venue;
package com.lugo.teams.reservs.application.dto.venue;

import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDetailDTO {
    private Long id;
    private String name;
    private String address;
    private String timeZone;
    private String mainPhotoUrl;
    private Double lat;
    private Double lng;
    private boolean active;

    private boolean allowOnsitePayment;
    private boolean allowBankTransfer;
    private boolean allowOnlinePayment;

    private List<String> photos;
    private OwnerSummaryDTO owner;
    private List<FieldSummaryDTO> fields;
}
