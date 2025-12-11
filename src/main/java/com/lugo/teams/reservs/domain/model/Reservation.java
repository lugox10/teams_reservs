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
        @Index(columnList = "user_name")
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

    // --- Relación con usuario registrado (opcional) ---
    // Ajusta el package/classname si tu entidad tiene otro nombre o package
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserv_user_id")
    private ReservUser reservUser;

    // --- Información alternativa para guest (si no hay reservUser) ---
    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "guest_email")
    private String guestEmail;

    // --- Legacy / compat: username string (opcional) ---
    // Algunos flujos / DTOs usan userName. Lo mantenemos opcional.
    @Column(name = "user_name")
    private String userName;

    private Long userId; // si necesitas referencia numérica adicional (opcional)

    // Campo reservado (campo físico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    // Timeslot opcional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    // duración en minutos (para form / validaciones). Máximo 60 en tu negocio.
    private Integer durationMinutes;

    private Integer playersCount = 1;

    private String teamName;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.NOT_INITIATED;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // referencia del pago (se usa en PaymentMapper / services)
    private String paymentReference;

    private String notes;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationTeamLink> teamLinks = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    // si tu BaseEntity ya tiene createdAt/updatedAt no declares de nuevo
    // private LocalDateTime createdAt;
}
