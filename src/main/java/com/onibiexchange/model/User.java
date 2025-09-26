package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", nullable = false, unique = true)
    @NonNull
    private String discordId;

    @Column(name = "username")
    @NonNull
    private String username;

    @Column(name = "balance", nullable = false)
    @NonNull
    private Integer balance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_work")
    private LocalDateTime lastWork;

    @Column(name = "xp")
    private int xp;

    @Column(name = "level")
    private int level;
}
