package com.lugo.teams.reservs.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
