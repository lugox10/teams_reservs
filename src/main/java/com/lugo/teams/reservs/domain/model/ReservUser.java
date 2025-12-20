package com.lugo.teams.reservs.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reserv_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone"),
                @UniqueConstraint(columnNames = "username")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = true)
    private String identification;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private ReservUserRole role;


    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean locked = false;
}
