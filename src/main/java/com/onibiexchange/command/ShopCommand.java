package com.onibiexchange.command;

import com.onibiexchange.model.User;
import com.onibiexchange.service.impl.ShopItemServiceImpl;
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
public class ShopCommand extends ListenerAdapter {

    private final ShopItemServiceImpl shopService;
    private final UserServiceImpl userService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        switch (command) {
            case "shop" -> handleShop(event);
            case "buy" -> handleBuy(event);
        }
    }

    private void handleShop(SlashCommandInteractionEvent event) {
        var items = shopService.listItems();

        if (items.isEmpty()) {
            event.reply("🛒 The shop is currently empty.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛒 OnibiExchange Shop")
                .setColor(Color.ORANGE);

        for (var item : items) {
            embed.addField(
                    "[" + item.getId() + "] " + item.getName(),
                    item.getDescription() + " — **" + item.getPrice() + "💰**",
                    false
            );
        }

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleBuy(SlashCommandInteractionEvent event) {
        long itemId = event.getOption("item") != null
                ? event.getOption("item").getAsLong()
                : -1;

        if (itemId == -1) {
            event.reply("❌ Please specify an item ID. Example: `/buy item:1`").queue();
            return;
        }

        User user = userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());
        String result = shopService.buyItem(user, itemId);
        event.reply(result).queue();
    }
}


