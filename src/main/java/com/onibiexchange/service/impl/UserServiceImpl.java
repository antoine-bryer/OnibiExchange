package com.onibiexchange.service.impl;

import com.onibiexchange.model.User;
import com.onibiexchange.repository.UserRepository;
import com.onibiexchange.service.IUserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private final Random random = new Random();

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(String id, String username) {
        return userRepository.findByDiscordId(id)
                .orElseGet(() -> userRepository.save(new User(id, username, 100)));
    }

    @Override
    public void save(User user){
        userRepository.save(user);
    }

    @Override
    public int getBalance(String discordId, String username) {
        User user = getOrCreateUser(discordId, username);
        return user.getBalance();
    }

    @Override
    public void updateBalance(User user, int amount) {
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
    }

    @Override
    public void updateBalanceAndCooldown(User user, int amount, int cooldown) {
        user.setBalance(user.getBalance() + amount);
        user.setLastWork(LocalDateTime.now().plusMinutes((long) cooldown));
        userRepository.save(user);
    }

    @Override
    public boolean hasEnoughBalance(String discordId, String username, int amount) {
        User user = getOrCreateUser(discordId, username);
        return user.getBalance() >= amount;
    }

    @Override
    public boolean canWork(User user) {
        if (user.getLastWork() == null) return true;
        long minutesSince = ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getLastWork());
        return minutesSince <= 0;
    }

    @Override
    public long cooldownRemaining(User user) {
        if (user.getLastWork() == null) return 0;
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getLastWork());
    }

    @Override
    public int work(User user, int minReward, int maxReward) {
        if (!canWork(user)) {
            throw new IllegalStateException("Cooldown active");
        }

        return minReward+random.nextInt(maxReward-minReward);
    }

    @Override
    public List<User> getLeaderboard() {
        return userRepository.getLeaderboard();
    }
}
