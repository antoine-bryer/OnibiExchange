package com.onibiexchange.commands;

import com.onibiexchange.models.RandomEvent;
import com.onibiexchange.models.User;
import com.onibiexchange.services.RandomEventService;
import com.onibiexchange.services.UserService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WorkCommand extends ListenerAdapter {

    private final UserService userService = new UserService();
    private final RandomEventService randomEventService = new RandomEventService();

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
            Random random = new Random();

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
            }
            reward = userService.work(user, minReward, maxReward);
            eb.addField("- Basic reward", "You earned **"+(reward != 0 ? reward+"** Onicoins !" : "NOTHING** !!!"), false);

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

            // Update user balance
            userService.updateBalance(user, reward, cooldown);

            if(reward > 0) {
                eb.addField("Result", "🔥 You worked hard and earned **"+reward+"** Onicoins !", false);
            } else if(reward == 0){
                eb.addField("Result", "💀 Bad luck! You earned **NOTHING**...", false);
            } else {
                eb.addField("Result", "💀 Bad luck! You lost **"+Math.abs(reward)+"** Onicoins...", false);
            }

            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        } catch (IllegalStateException e) {
            event.reply("❌ You are still on cooldown !").setEphemeral(true).queue();
        }
    }
}
