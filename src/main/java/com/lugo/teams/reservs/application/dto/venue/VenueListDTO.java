// src/main/java/com/lugo/teams/reservs/application/dto/venue/VenueListDTO.java
package com.lugo.teams.reservs.application.dto.venue;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueListDTO {
    private Long id;
    private String name;
    private String mainPhotoUrl;
    private String address;
    private Double lat;
    private Double lng;
    private boolean active;
    private boolean allowOnsitePayment;
    private boolean allowBankTransfer;
    private boolean allowOnlinePayment;
    private Integer fieldsCount;
}
