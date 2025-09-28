package com.onibiexchange;

import com.onibiexchange.command.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);

        try {
            String token = System.getenv("DISCORD_TOKEN");

            if (token == null || token.isEmpty()) {
                System.err.println("❌ ERROR: DISCORD_TOKEN environment variable is not set.");
                System.exit(1);
            }

            System.out.println("✅ Starting OnibiExchange bot...");

            LeaderboardCommand leaderboardCommand = context.getBean(LeaderboardCommand.class);
            ProfileCommand profileCommand = context.getBean(ProfileCommand.class);
            SlotsCommand slotsCommand = context.getBean(SlotsCommand.class);
            WorkCommand workCommand = context.getBean(WorkCommand.class);
            ShopCommand shopCommand = context.getBean(ShopCommand.class);
            InventoryCommand inventoryCommand = context.getBean(InventoryCommand.class);

            JDABuilder.createDefault(token)
                    .addEventListeners(leaderboardCommand, profileCommand, slotsCommand, workCommand, shopCommand, inventoryCommand)
                    .setActivity(Activity.playing("OnibiExchange"))
                    .build()
                    .updateCommands()
                    .addCommands(
                            Commands.slash("profile", "Show your OnibiExchange profile"),
                            Commands.slash("work", "Earn Onicoins with cooldown"),
                            Commands.slash("leaderboard", "Show the leaderboard of the server"),
                            Commands.slash("slots", "GAMBLING !!!")
                                    .addOption(OptionType.INTEGER, "bet", "The amount of Onicoins you want to bet", true),
                            Commands.slash("shop", "Show OnibiExchange shop"),
                            Commands.slash("buy", "Buy an item from the shop")
                                    .addOption(OptionType.INTEGER, "item", "Shop item ID", true),
                            Commands.slash("inventory", "Show your owned items")
                    )
                    .queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

}
