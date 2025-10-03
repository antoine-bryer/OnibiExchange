package com.onibiexchange.command;

import com.onibiexchange.service.impl.LotteryServiceImpl;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class LotteryCommand extends ListenerAdapter {
    private final LotteryServiceImpl lotteryService;
    private final UserServiceImpl userService; // si tu en as besoin pour créer l'user à la volée

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("lottery")) return;

        String sub = event.getSubcommandName();
        if (sub == null) {
            event.reply("❓ Use `/lottery enter` or `/lottery status`.").setEphemeral(true).queue();
            return;
        }

        switch (sub) {
            case "enter" -> {
                int number = event.getOption("number").getAsInt();
                int amount = event.getOption("amount").getAsInt();

                // s'assurer que l'utilisateur existe (si ton UserService gère getOrCreateUser)
                userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());

                String res = lotteryService.enterBet(event.getUser().getId(), number, amount);
                event.reply(res).setEphemeral(true).queue();
            }
            case "status" -> {
                String status = lotteryService.getStatus();
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("🎰 Weekly Lottery")
                        .setColor(Color.MAGENTA)
                        .setDescription(status);
                event.replyEmbeds(eb.build()).queue();
            }
            default -> event.reply("❓ Unknown subcommand.").setEphemeral(true).queue();
        }
    }
}
