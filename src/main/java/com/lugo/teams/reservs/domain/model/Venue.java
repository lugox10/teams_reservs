package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String timeZone;

    @Builder.Default
    private boolean active = true;

    /**
     * Imagen principal / logo para cards.
     */
    private String mainPhotoUrl;

    /**
     * Coordenadas para mapa (opcional).
     */
    private Double lat;
    private Double lng;

    /**
     * Opciones de pago que el owner habilita por venue.
     */
    @Builder.Default
    private boolean allowOnsitePayment = true;

    @Builder.Default
    private boolean allowBankTransfer = true;

    @Builder.Default
    private boolean allowOnlinePayment = false;

    @ElementCollection
    @CollectionTable(name = "venue_photos", joinColumns = @JoinColumn(name = "venue_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photos = new ArrayList<>(); // galería

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Field> fields = new ArrayList<>();
}
