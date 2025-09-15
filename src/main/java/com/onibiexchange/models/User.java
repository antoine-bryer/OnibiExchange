package com.onibiexchange.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", nullable = false, unique = true)
    private String discordId;

    @Column(name = "username")
    private String username;

    @Column(name = "balance", nullable = false)
    private int balance = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_work")
    private LocalDateTime lastWork;

    @Column(name = "xp")
    private int xp;

    @Column(name = "level")
    private int level;

    public User() {}
    public User(String discordId, String username) {
        this.discordId = discordId;
        this.username = username;
        this.balance = 100; // solde initial
        this.xp = 0;
        this.level = 1;
    }

    public Long getId() {
        return id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastWork() {
        return lastWork;
    }

    public void setLastWork(LocalDateTime lastWork) {
        this.lastWork = lastWork;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
