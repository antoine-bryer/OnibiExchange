package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slots implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int jackpot;

}
