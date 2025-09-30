package com.onibiexchange.command;

import com.onibiexchange.model.User;
import com.onibiexchange.model.UserItem;
import com.onibiexchange.service.impl.ItemEffectServiceImpl;
import com.onibiexchange.service.impl.UserServiceImpl;
import com.onibiexchange.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UseCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final UserItemRepository userItemRepository;
    private final ItemEffectServiceImpl itemEffectService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("use")) {
            String discordId = event.getUser().getId();
            String username = event.getUser().getName();
            User user = userService.getOrCreateUser(discordId, username);

            List<UserItem> items = userItemRepository.findByUser(user);
            if (items.isEmpty()) {
                event.reply("📦 Your inventory is empty. Buy items with `/shop`.").setEphemeral(true).queue();
                return;
            }

            StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("use-item-menu")
                    .setPlaceholder("Choose an item to use...");

            for (UserItem ui : items) {
                String label = ui.getItem().getName() + " (x" + ui.getQuantity() + ")";
                String value = ui.getId().toString();
                String description = ui.getItem().getDescription();
                menuBuilder.addOption(label, value, description);
            }

            StringSelectMenu menu = menuBuilder.build();
            event.reply("Select an item to use:").addActionRow(menu).setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("use-item-menu")) {
            event.deferEdit().queue();

            String value = event.getValues().get(0);
            long userItemId;
            try {
                userItemId = Long.parseLong(value);
            } catch (NumberFormatException e) {
                event.reply("❌ Invalid selection.").setEphemeral(true).queue();
                return;
            }

            String discordId = event.getUser().getId();
            String result = itemEffectService.useItem(userItemId, discordId);

            event.getHook().editOriginal(result)
                    .setComponents()
                    .queue();
        }
    }
}
