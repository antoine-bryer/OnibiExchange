package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_items")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false)
    private ShopEffectType effectType;
}
