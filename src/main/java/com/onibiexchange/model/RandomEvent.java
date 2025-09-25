package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "random_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private int value;

    @Column(nullable = false)
    private int rarity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

}
