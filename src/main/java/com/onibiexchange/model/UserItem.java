package com.onibiexchange.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "item_id"})
})
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(nullable = false)
    private int quantity;
}
