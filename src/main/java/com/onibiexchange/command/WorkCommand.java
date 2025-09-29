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

import java.time.LocalDateTime;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class WorkCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final RandomEventServiceImpl randomEventService;
    private final LevelServiceImpl levelService;
    private final UserBuffRepository userBuffRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("work")) {
            String discordId = event.getUser().getId();
            String username = event.getUser().getName();

            User user = userService.getOrCreateUser(discordId, username);

            if (userService.canWork(user)) {
                event.reply("Choose your path")
                        .addActionRow(
                                Button.primary("easy", "Easy"),
                                Button.secondary("medium", "Medium"),
                                Button.danger("hard", "Hard")
                        ).queue();
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

            int reward;
            int cooldown = 0;
            int minReward = 0;
            int maxReward = 0;
            int xpGain = 0;
            Random random = new Random();
            StringBuilder result = new StringBuilder();

            // Making the embed message
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("WORK");
            eb.setAuthor(user.getUsername(), null, event.getUser().getAvatarUrl());
            eb.setDescription("It's time to make money !");

            // Initial reward depending on the path chosen
            if(event.getComponentId().equalsIgnoreCase("easy")) {
                minReward = 10;
                maxReward = 50;
                cooldown = 1;
                xpGain = 15;
            } else if (event.getComponentId().equalsIgnoreCase("medium")) {
                int chance = random.nextInt(100);
                if(chance < 40) {
                    minReward = 50;
                    maxReward = 100;
                } else {
                    minReward = 10;
                    maxReward = 20;
                }
                cooldown = 5;
                xpGain = 30;
            } else if (event.getComponentId().equalsIgnoreCase("hard")) {
                int chance = random.nextInt(100);
                if(chance < 10) {
                    minReward = 300;
                    maxReward = 500;
                } else {
                    minReward = 1;
                    maxReward = 10;
                }
                cooldown = 15;
                xpGain = 50;
            }
            reward = userService.work(user, minReward, maxReward);
            eb.addField("- Basic reward", "You earned **"+(reward != 0 ? reward+"** Onicoins !" : "NOTHING** !!!"), false);

            // Items effects
            java.util.List<UserBuff> buffs = userBuffRepository.findByUser(user);
            boolean doubleApplied = false;
            for (UserBuff buff : buffs) {
                if (buff.getEffectType().equals("WORK_BOOST_20_PERCENT")) {
                    if (buff.getExpirationDate() != null && buff.getExpirationDate().isAfter(LocalDateTime.now())) {
                        reward = (int) Math.round(reward * 1.2);
                        eb.addField("- Boost 20%", "Your reward has been increased by 20% !", false);
                    } else {
                        userBuffRepository.delete(buff);
                    }
                }
                if (buff.getEffectType().equals("RANDOM_BONUS_ON_WORK") && buff.getRemainingUses() > 0) {
                    if (random.nextInt(100) < 5) {
                        int bonus = 100 + random.nextInt(401);
                        reward += bonus;
                        eb.addField("- Bonus aléatoire", "You have earned a bonus of "+bonus+" Onicoins !", false);
                    }
                    buff.setRemainingUses(buff.getRemainingUses() - 1);
                    if (buff.getRemainingUses() <= 0) {
                        userBuffRepository.delete(buff);
                    } else {
                        userBuffRepository.save(buff);
                    }
                }
                if (!doubleApplied && buff.getEffectType().equals("DOUBLE_WORK_REWARD")) {
                    reward *= 2;
                    eb.addField("- Double Reward", "Your reward has been doubled !", false);
                    userBuffRepository.delete(buff);
                    doubleApplied = true;
                }
            }

            // Random Event
            RandomEvent randomEvent = randomEventService.getRandomEvent();
            if(randomEvent != null){
                switch (randomEvent.getType()){
                    case "BONUS":
                        reward += randomEvent.getValue();
                        break;
                    case "MALUS":
                        reward = reward - randomEvent.getValue();
                        break;
                    case "TREASURE":
                        reward = reward * randomEvent.getValue();
                        break;
                    case "ROBBERY":
                        if(reward != 0)
                            reward = reward / randomEvent.getValue();
                        break;
                    case "KOSEN":
                        reward = Math.negateExact(user.getBalance());
                        break;
                    default:
                        break;
                }
                eb.addField("- Random Event", randomEvent.getDescription(), false);
            }

            // Add xp and manage level up
            levelService.addXp(user, xpGain);

            if(reward > 0) {
                result.append("🔥 You worked hard and earned **").append(reward).append("** Onicoins !\n");
            } else if(reward == 0){
                result.append("💀 Bad luck! You earned **NOTHING**...\n");
            } else {
                result.append("💀 Bad luck! You lost **").append(Math.abs(reward)).append("** Onicoins...\n");
            }
            result.append("✨ You gained **").append(xpGain).append(" XP**.\n");
            result.append("📊 Level: ").append(user.getLevel()).append(" (").append(user.getXp())
                    .append("/").append(levelService.xpToNextLevel(user.getLevel())).append(" XP)");
            eb.addField("Result", result.toString(), false);

            // Update user
            userService.updateBalanceAndCooldown(user, reward, cooldown);

            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        } catch (IllegalStateException e) {
            event.reply("❌ You are still on cooldown !").setEphemeral(true).queue();
        }
    }
}
