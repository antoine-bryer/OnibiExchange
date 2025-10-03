package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lottery_draws", uniqueConstraints = @UniqueConstraint(columnNames = "draw_date"))
@Data @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LotteryDraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Draw date (Sat 00:00 of the current week)
    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;

    @Column(name = "winning_number")
    private Integer winningNumber; // null while not drawn

    @Column(name = "total_pot", nullable = false)
    private long totalPot;

    @Column(name = "distributed", nullable = false)
    private boolean distributed; // true after winnings are distributed
}
