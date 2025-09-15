package com.onibiexchange.commands;

import com.onibiexchange.models.User;
import com.onibiexchange.services.LevelService;
import com.onibiexchange.services.UserService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ProfileCommand extends ListenerAdapter {

    private final UserService userService = new UserService();
    private final LevelService levelService = new LevelService();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("profile")) {
            // Get user
            User user = userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());

            // Create Embed
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("OnibiExchange Profile - **"+event.getUser().getEffectiveName().toUpperCase()+"**");
            eb.setColor(new Color(50, 72, 168));
            eb.setThumbnail(event.getUser().getEffectiveAvatarUrl());

            eb.addField("💰 Balance", user.getBalance() + " Onicoins", true);
            eb.addField("⭐ Level", String.valueOf(user.getLevel()), true);
            eb.addField("✨ XP", user.getXp() + " / " + levelService.xpToNextLevel(user.getLevel()), true);

            eb.setFooter("Keep working to grow stronger, " + event.getUser().getName() + "!", null);

            event.replyEmbeds(eb.build()).queue();
        }
    }

}
