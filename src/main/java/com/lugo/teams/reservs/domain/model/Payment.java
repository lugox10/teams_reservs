package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(columnList = "payment_reference"),
        @Index(columnList = "reservation_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reserva asociada a este pago.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    /**
     * Monto de este pago (total o parcial).
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Estado del pago según nuestro flujo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * Referencia única del pago en el gateway externo
     * (o en nuestro propio sistema).
     */
    @Column(name = "payment_reference", unique = true)
    private String paymentReference;

    /**
     * Opcional: nombre del proveedor de pago (ej: Wompi, Stripe, MercadoPago).
     */
    private String provider;

    /**
     * true si es un reembolso / refund.
     */
    private boolean refund;

    /**
     * Texto libre para logs / mensajes de error.
     */
    private String message;
}
