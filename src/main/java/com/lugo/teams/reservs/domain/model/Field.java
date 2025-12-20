package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ej: "Cancha A - Sintética"

    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Enumerated(EnumType.STRING)
    private SurfaceType surface;

    private Integer capacityPlayers;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Builder.Default
    private boolean active = true;

    /**
     * Duración en minutos del slot base (por defecto 60).
     */
    @Builder.Default
    private Integer slotMinutes = 60;

    /**
     * Hora de apertura (entera 0..23).
     */
    @Builder.Default
    private Integer openHour = 6;

    /**
     * Hora de cierre (entera 0..23). Convención: closeHour es exclusivo si lo quieres así.
     */
    @Builder.Default
    private Integer closeHour = 23;

    /**
     * Mínimo de horas/slots a reservar (en unidades de slotMinutes).
     */
    @Builder.Default
    private Integer minBookingHours = 1;

    @ElementCollection
    @CollectionTable(name = "field_photos", joinColumns = @JoinColumn(name = "field_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photos = new ArrayList<>(); // opcional field

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;


}
