package com.onibiexchange.service.impl;

import com.onibiexchange.model.*;
import com.onibiexchange.model.ShopEffectType;
import com.onibiexchange.repository.*;
import com.onibiexchange.service.IItemEffectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemEffectServiceImpl implements IItemEffectService {

    private final UserRepository userRepository;
    private final ShopItemRepository shopItemRepository;
    private final UserItemRepository userItemRepository;
    private final UserBuffRepository userBuffRepository;

    private final Random rng = new Random();

    @Override
    public String useItem(Long userItemId, String discordId) {
        // Find user
        Optional<User> optUser = userRepository.findByDiscordId(discordId);
        if (optUser.isEmpty()) return "❌ User not found.";
        User user = optUser.get();

        // Find item in inventory
        Optional<UserItem> optUI = userItemRepository.findByIdAndUser(userItemId, user);
        if (optUI.isEmpty()) return "❌ Item not found in your inventory.";
        UserItem userItem = optUI.get();

        ShopItem shopItem = userItem.getItem();
        ShopEffectType effectType = shopItem.getEffectType();

        // Clean expired buffs BEFORE applying new effect
        cleanupExpiredBuffs(user);

        // Prevent duplicate effect usage
        if (hasActiveBuff(user, effectType)) {
            return "⚠️ This effect is already active!";
        }

        String resultMessage;

        switch (effectType) {
            case RESET_WORK_COOLDOWN:
                user.setLastWork(null);
                userRepository.save(user);
                resultMessage = "🔄 Your /work cooldown has been reset.";
                break;

            case DOUBLE_WORK_REWARD:
                userBuffRepository.save(UserBuff.builder()
                        .user(user)
                        .effectType(effectType.name())
                        .remainingUses(1)
                        .createdAt(LocalDateTime.now())
                        .build());
                resultMessage = "✨ Double Loot activated for your next /work !";
                break;

            case WORK_BOOST_20_PERCENT:
                userBuffRepository.save(UserBuff.builder()
                        .user(user)
                        .effectType(effectType.name())
                        .remainingUses(Integer.MAX_VALUE)
                        .expirationDate(LocalDateTime.now().plusHours(24))
                        .createdAt(LocalDateTime.now())
                        .build());
                resultMessage = "🔥 +20% work boost active for 24 hours on /work !";
                break;

            case RANDOM_BONUS_ON_WORK:
                userBuffRepository.save(UserBuff.builder()
                        .user(user)
                        .effectType(effectType.name())
                        .remainingUses(7)
                        .createdAt(LocalDateTime.now())
                        .build());
                resultMessage = "🎲 5% chance of winning a random bonus with each /work for 7 uses !";
                break;

            default:
                resultMessage = "❌ Unknown item effect.";
        }

        // Consume one quantity
        consumeItem(userItem);

        return resultMessage;
    }

    private void cleanupExpiredBuffs(User user) {
        List<UserBuff> expiredBuffs = userBuffRepository.findByUserAndExpirationDateBefore(user, LocalDateTime.now());
        if (!expiredBuffs.isEmpty()) {
            userBuffRepository.deleteAll(expiredBuffs);
        }
    }

    private boolean hasActiveBuff(User user, ShopEffectType effectType) {
        return userBuffRepository.existsByUserAndEffectType(user, effectType.name());
    }

    private void consumeItem(UserItem userItem) {
        int qty = userItem.getQuantity();
        if (qty > 1) {
            userItem.setQuantity(qty - 1);
            userItemRepository.save(userItem);
        } else {
            userItemRepository.delete(userItem);
        }
    }
}
