package com.lugo.teams.reservs.application.dto.owner;
import lombok.*;

/**
 * DTO de respuesta para Owner. Incluye información del ReservUser ligado cuando existe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String nit;
    private String businessName;
    private String logo;

    // Si el Owner está vinculado a un ReservUser, exponemos su id y username
    private Long userId;
    private String username;
}
