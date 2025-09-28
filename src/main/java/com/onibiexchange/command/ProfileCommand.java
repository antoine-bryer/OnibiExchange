package com.onibiexchange.command;

import com.onibiexchange.model.User;
import com.onibiexchange.service.impl.LevelServiceImpl;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class ProfileCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final LevelServiceImpl levelService;

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
