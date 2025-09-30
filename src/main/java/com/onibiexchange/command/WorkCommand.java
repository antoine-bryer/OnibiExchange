package com.onibiexchange.command;

import com.onibiexchange.model.RandomEvent;
import com.onibiexchange.model.User;
import com.onibiexchange.model.UserBuff;
import com.onibiexchange.repository.UserBuffRepository;
import com.onibiexchange.service.impl.LevelServiceImpl;
import com.onibiexchange.service.impl.RandomEventServiceImpl;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class WorkCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final RandomEventServiceImpl randomEventService;
    private final LevelServiceImpl levelService;
    private final UserBuffRepository userBuffRepository;

    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("work")) {
            String discordId = event.getUser().getId();
            String username = event.getUser().getName();

            User user = userService.getOrCreateUser(discordId, username);

            if (userService.canWork(user)) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("⚒️ **Choose Your Path, Onibi Worker!**");

                StringBuilder description = new StringBuilder().append("Your spirit awakens... but how far will you push it?\n\n")
                        .append("🪵 **Wandering Spirit** — Safe but humble gains\n")
                        .append("🔥 **Fierce Flame** — Greater risk, greater reward\n")
                        .append("👹 **Wrath of the Onibi** — Only the fearless dare enter...");
                eb.setDescription(description.toString());

                eb.setColor(Color.RED);
                eb.setFooter("Make your choice wisely...", event.getUser().getAvatarUrl());

                event.replyEmbeds(eb.build())
                        .addActionRow(
                                Button.primary("easy", "🪵 Wandering Spirit"),
                                Button.secondary("medium", "🔥 Fierce Flame"),
                                Button.danger("hard", "👹 Wrath of the Onibi")
                        )
                        .queue();
            } else {
                long remaining = userService.cooldownRemaining(user);
                event.reply("⏳ You must wait " + remaining + " seconds before working again.")
                        .setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String discordId = event.getUser().getId();
        String username = event.getUser().getName();

        event.deferEdit().queue();
        event.getMessage().delete().queue();

        try {
            User user = userService.getOrCreateUser(discordId, username);
            int[] workParams = getWorkParameters(event.getComponentId());
            int reward = userService.work(user, workParams[1], workParams[2]); // min, max
            int cooldown = workParams[0];
            int xpGain = workParams[3];

            EmbedBuilder eb = createBaseEmbed(event, user);
            eb.addField("- Basic reward", getBasicRewardText(reward), false);

            reward = applyBuffs(user, reward, eb);
            reward = applyRandomEvent(reward, eb);

            levelService.addXp(user, xpGain);
            appendFinalResult(eb, reward, xpGain, user);

            userService.updateBalanceAndCooldown(user, reward, cooldown);
            event.getChannel().sendMessageEmbeds(eb.build()).queue();

        } catch (IllegalStateException e) {
            event.reply("❌ You are still on cooldown !").setEphemeral(true).queue();
        }
    }

    private int[] getWorkParameters(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> new int[]{1, 10, 50, 15}; // cooldown, min, max, xp
            case "medium" -> (random.nextInt(100) < 40) ? new int[]{5, 50, 100, 30} : new int[]{5, 10, 20, 30};
            case "hard" -> (random.nextInt(100) < 10) ? new int[]{15, 300, 500, 50} : new int[]{15, 1, 10, 50};
            default -> throw new IllegalStateException("Invalid difficulty");
        };
    }

    private EmbedBuilder createBaseEmbed(ButtonInteractionEvent event, User user) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("WORK");
        eb.setAuthor(user.getUsername(), null, event.getUser().getAvatarUrl());
        eb.setDescription("It's time to make money !");
        eb.setColor(Color.RED);
        return eb;
    }

    private String getBasicRewardText(int reward) {
        return "You earned **" + (reward != 0 ? reward + "** Onicoins !" : "NOTHING** !!!");
    }

    private int applyBuffs(User user, int reward, EmbedBuilder eb) {
        List<UserBuff> buffs = userBuffRepository.findByUser(user);
        boolean doubleApplied = false;

        for (UserBuff buff : buffs) {
            reward = applySingleBuff(buff, reward, eb, random);
            if (buff.getEffectType().equals("DOUBLE_WORK_REWARD") && !doubleApplied) {
                doubleApplied = true;
            }
        }
        return reward;
    }

    private int applySingleBuff(UserBuff buff, int reward, EmbedBuilder eb, Random random) {
        return switch (buff.getEffectType()) {
            case "WORK_BOOST_20_PERCENT" -> applyWorkBoost(buff, reward, eb);
            case "RANDOM_BONUS_ON_WORK" -> applyRandomBonus(buff, reward, eb, random);
            case "DOUBLE_WORK_REWARD" -> applyDoubleReward(buff, reward, eb);
            default -> reward;
        };
    }

    private int applyWorkBoost(UserBuff buff, int reward, EmbedBuilder eb) {
        if (buff.getExpirationDate() != null && buff.getExpirationDate().isAfter(LocalDateTime.now())) {
            eb.addField("- Boost 20%", "Your reward has been increased by 20% !", false);
            return (int) Math.round(reward * 1.2);
        }
        userBuffRepository.delete(buff);
        return reward;
    }

    private int applyRandomBonus(UserBuff buff, int reward, EmbedBuilder eb, Random random) {
        if (buff.getRemainingUses() > 0) {
            if (random.nextInt(100) < 5) {
                int bonus = 100 + random.nextInt(401);
                reward += bonus;
                eb.addField("- Random Bonus", "You gained " + bonus + " Onicoins!", false);
            }
            updateBuffUsage(buff);
        }
        return reward;
    }

    private void updateBuffUsage(UserBuff buff) {
        buff.setRemainingUses(buff.getRemainingUses() - 1);
        if (buff.getRemainingUses() <= 0) userBuffRepository.delete(buff);
        else userBuffRepository.save(buff);
    }

    private int applyDoubleReward(UserBuff buff, int reward, EmbedBuilder eb) {
        eb.addField("- Double Reward", "Your reward has been doubled !", false);
        userBuffRepository.delete(buff);
        return reward * 2;
    }

    private int applyRandomEvent(int reward, EmbedBuilder eb) {
        RandomEvent event = randomEventService.getRandomEvent();
        if (event == null) return reward;

        switch (event.getType()) {
            case "BONUS": reward += event.getValue(); break;
            case "MALUS": reward -= event.getValue(); break;
            case "TREASURE": reward *= event.getValue(); break;
            case "ROBBERY": reward = reward != 0 ? reward / event.getValue() : reward; break;
            case "KOSEN": reward = -Math.abs(reward); break;
            default: return reward;
        }
        eb.addField("- Random Event", event.getDescription(), false);
        return reward;
    }

    private void appendFinalResult(EmbedBuilder eb, int reward, int xpGain, User user) {
        StringBuilder result = new StringBuilder();
        if (reward > 0) result.append("🔥 You earned **").append(reward).append("** Onicoins!\n");
        else if (reward == 0) result.append("💀 You earned **NOTHING**...\n");
        else result.append("💀 You lost **").append(Math.abs(reward)).append("** Onicoins...\n");

        result.append("✨ You gained **").append(xpGain).append(" XP**.\n");
        result.append("📊 Level: ").append(user.getLevel()).append(" (")
                .append(user.getXp()).append("/").append(levelService.xpToNextLevel(user.getLevel())).append(" XP)");

        eb.addField("Result", result.toString(), false);
    }
}
