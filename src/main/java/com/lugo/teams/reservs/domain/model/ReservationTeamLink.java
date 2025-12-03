package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_team_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationTeamLink extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referencia a la reserva local
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // datos para integrar con Teams FC (opcional)
    private Long teamsFcMatchId;   // si se creó un match en Teams FC
    private String teamsFcUrl;     // link a la pantalla del match en Teams FC

    private String teamName; // nombre del equipo asociado (si aplica)


}
