package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "owners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Owner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String nit;
    private String businessName;
    private String logo;

    /**
     * Si ya habías añadido password en Owner lo dejamos (opcional).
     * Recomendado: la auth principal la maneja ReservUser; Owner.user referencia al ReservUser.
     */
    private String password;

    /**
     * Vinculo opcional al usuario de autenticación
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ReservUser user;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Venue> venues = new ArrayList<>();
}
