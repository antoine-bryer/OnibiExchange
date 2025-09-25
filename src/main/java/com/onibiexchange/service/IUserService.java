package com.onibiexchange.service;

import com.onibiexchange.model.User;

import java.util.List;

public interface IUserService {
    public User getOrCreateUser(String id, String username);

    public void save(User user);

    public int getBalance(String discordId, String username);

    public void updateBalance(User user, int amount);

    public void updateBalanceAndCooldown(User user, int amount, int cooldown);

    public boolean hasEnoughBalance(String discordId, String username, int amount);

    public boolean canWork(User user);

    public long cooldownRemaining(User user);

    public int work(User user, int minReward, int maxReward);

    public List<User> getLeaderboard();
}
