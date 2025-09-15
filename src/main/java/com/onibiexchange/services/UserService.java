package com.onibiexchange.services;

import com.onibiexchange.models.User;
import com.onibiexchange.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

public class UserService {

    private final UserRepository userRepository = new UserRepository();
    private final Random random = new Random();

    public User getOrCreateUser(String discordId, String username) {
        User user = userRepository.findByDiscordId(discordId);
        if (user == null) {
            user = new User(discordId, username);
            userRepository.save(user);
        }
        return user;
    }

    public void save(User user){
        userRepository.save(user);
    }

    public int getBalance(String discordId, String username) {
        User user = getOrCreateUser(discordId, username);
        return user.getBalance();
    }

    public void updateBalance(User user, int amount) {
        user.setBalance(user.getBalance() + amount);

        userRepository.save(user);
    }

    public void updateBalanceAndCooldown(User user, int amount, int cooldown) {
        user.setBalance(user.getBalance() + amount);
        user.setLastWork(LocalDateTime.now().plusMinutes((long) cooldown));

        userRepository.save(user);
    }

    public boolean hasEnoughBalance(String discordId, String username, int amount) {
        User user = getOrCreateUser(discordId, username);
        return user.getBalance() >= amount;
    }

    public boolean canWork(User user) {
        if (user.getLastWork() == null) return true;
        long minutesSince = ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getLastWork());
        return minutesSince <= 0;
    }

    public long cooldownRemaining(User user) {
        if (user.getLastWork() == null) return 0;
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getLastWork());
    }

    public int work(User user, int minReward, int maxReward) {
        if (!canWork(user)) {
            throw new IllegalStateException("Cooldown active");
        }

        return minReward+random.nextInt(maxReward-minReward);
    }

    public List<User> getLeaderboard(){
        return userRepository.getLeaderboard();
    }

}
