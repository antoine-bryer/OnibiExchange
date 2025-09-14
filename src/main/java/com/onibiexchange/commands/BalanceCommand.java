package com.onibiexchange.commands;

import com.onibiexchange.services.UserService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand extends ListenerAdapter {
    private final UserService userService = new UserService();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equalsIgnoreCase("balance")){

            String discordId = event.getUser().getId();
            String username = event.getUser().getName();

            int balance = userService.getBalance(discordId, username);

            event.reply("💰 **" + username + "** has " + balance + " Onicoins.")
                    .queue();
        }
    }
}
