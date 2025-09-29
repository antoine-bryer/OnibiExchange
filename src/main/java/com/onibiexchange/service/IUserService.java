package com.onibiexchange.service;

import com.onibiexchange.model.User;

import java.util.List;

public interface IUserService {
    User getOrCreateUser(String id, String username);
    void save(User user);
    int getBalance(String discordId, String username);
    void updateBalance(User user, int amount);
    void updateBalanceAndCooldown(User user, int amount, int cooldown);
    boolean hasEnoughBalance(String discordId, String username, int amount);
    boolean canWork(User user);
    long cooldownRemaining(User user);
    int work(User user, int minReward, int maxReward);
    List<User> getLeaderboard();
}
