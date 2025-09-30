package com.onibiexchange.command;

import com.onibiexchange.model.ShopItem;
import com.onibiexchange.model.User;
import com.onibiexchange.model.UserItem;
import com.onibiexchange.repository.ShopItemRepository;
import com.onibiexchange.repository.UserItemRepository;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ShopCommand extends ListenerAdapter {

    private final ShopItemRepository shopItemRepository;
    private final UserItemRepository userItemRepository;
    private final UserServiceImpl userService;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("shop")) return;

        String discordId = event.getUser().getId();
        String userName = event.getUser().getName();
        User user = userService.getOrCreateUser(discordId, userName);

        String itemName = event.getOption("item") != null ? Objects.requireNonNull(event.getOption("item")).getAsString() : null;
        int quantity = event.getOption("quantity") != null ? Objects.requireNonNull(event.getOption("quantity")).getAsInt() : 1;

        if (itemName == null) {
            sendShopList(event);
            return;
        }

        ShopItem shopItem = shopItemRepository.findAll().stream()
                .filter(i -> i.getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);

        if (shopItem == null) {
            event.reply("❌ Item not found. Use `/shop` without arguments to view items.").setEphemeral(true).queue();
            return;
        }

        int totalCost = shopItem.getPrice() * quantity;
        if (user.getBalance() < totalCost) {
            event.reply("❌ You don't have enough coins! Cost: " + totalCost + " | Your balance: " + user.getBalance())
                    .setEphemeral(true).queue();
            return;
        }

        user.setBalance(user.getBalance() - totalCost);
        userService.save(user);

        UserItem userItem = userItemRepository.findByUserAndItem(user, shopItem)
                .orElse(UserItem.builder().user(user).item(shopItem).quantity(0).build());

        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        event.reply("✅ Purchased **" + quantity + "x " + shopItem.getName() + "** for **" + totalCost + "** coins!")
                .setEphemeral(true).queue();
    }

    private void sendShopList(SlashCommandInteractionEvent event) {
        List<ShopItem> items = shopItemRepository.findAll();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛒 OnibiExchange Shop")
                .setDescription("Use `/shop item:<name> quantity:<amount>` to buy.")
                .setColor(Color.ORANGE);

        for (ShopItem item : items) {
            embed.addField(
                    "[" + item.getId() + "] " + item.getName(),
                    item.getDescription() + " — **" + item.getPrice() + " 💰**",
                    false
            );
        }

        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("shop")) return;

        if (event.getFocusedOption().getName().equals("item")) {
            List<Choice> choices = shopItemRepository.findAll().stream()
                    .map(i -> new Choice(i.getName(), i.getName()))
                    .toList();
            event.replyChoices(choices).queue();
        }
    }
}
