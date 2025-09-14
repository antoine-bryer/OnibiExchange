package com.onibiexchange.models;

import jakarta.persistence.*;

@Entity
@Table(name = "random_event")
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

    public RandomEvent() {}
    public RandomEvent(String type, int value, int rarity, String description) {
        this.type = type;
        this.value = value;
        this.rarity = rarity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    public int getRarity() {
        return rarity;
    }
    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
