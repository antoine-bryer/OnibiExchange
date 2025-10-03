package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lottery_bets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "draw_date"}))
@Data @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LotteryBet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Draw date (Sat 00:00 of the current week)
    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "chosen_number", nullable = false)
    private int chosenNumber; // 1..10

    @Column(nullable = false)
    private long amount; // bet
}
