package com.onibiexchange.command;

import com.onibiexchange.model.User;
import com.onibiexchange.model.UserItem;
import com.onibiexchange.repository.UserItemRepository;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final UserItemRepository userItemRepository;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("inventory")) {
            User user = userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());
            List<UserItem> inventory = userItemRepository.findByUser(user);

            if (inventory.isEmpty()) {
                event.reply("📦 Your inventory is empty. Go buy something with `/shop`.").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📦 Inventory of " + event.getUser().getName())
                    .setColor(Color.CYAN);

            for (UserItem item : inventory) {
                embed.addField(item.getItem().getName(),
                        "x" + item.getQuantity() + " — " + item.getItem().getDescription(),
                        false);
            }

            event.replyEmbeds(embed.build()).queue();
        }
    }
}
