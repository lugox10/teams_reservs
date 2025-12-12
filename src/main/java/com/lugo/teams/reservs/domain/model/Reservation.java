package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations", indexes = {
        @Index(columnList = "field_id, time_slot_id"),
        @Index(columnList = "user_name"),
        @Index(columnList = "venue_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con usuario registrado (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserv_user_id")
    private ReservUser reservUser;

    // Información alternativa para guest (si no hay reservUser)
    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "guest_email")
    private String guestEmail;

    // Legacy / compat: username string (opcional)
    @Column(name = "user_name")
    private String userName;

    /**
     * Campo opcional denormalizado: venue asociado (mejora queries owner->reservations).
     */
    @Column(name = "venue_id")
    private Long venueId;

    private Long userId; // referencia numérica adicional (opcional)

    // Campo reservado (campo físico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    // Timeslot opcional (si estás usando time_slots persistentes)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    // duración en minutos (para form / validaciones). Máximo 60 por slot (o multiples)
    private Integer durationMinutes;

    @Builder.Default
    private Integer playersCount = 1;

    private String teamName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.NOT_INITIATED;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // referencia del pago (se usa en PaymentMapper / services)
    private String paymentReference;

    private String notes;

    /**
     * Método de pago elegido por usuario: ONSITE | BANK | ONLINE
     */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationTeamLink> teamLinks = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
