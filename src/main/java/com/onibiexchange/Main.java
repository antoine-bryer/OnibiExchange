package com.onibiexchange;

import com.onibiexchange.command.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);

        try {
            String token = System.getenv("DISCORD_TOKEN");

            if (token == null || token.isEmpty()) {
                logger.error("❌ ERROR: DISCORD_TOKEN environment variable is not set.");
                System.exit(1);
            }

            logger.info("✅ Starting OnibiExchange bot...");

            LeaderboardCommand leaderboardCommand = context.getBean(LeaderboardCommand.class);
            ProfileCommand profileCommand = context.getBean(ProfileCommand.class);
            SlotsCommand slotsCommand = context.getBean(SlotsCommand.class);
            WorkCommand workCommand = context.getBean(WorkCommand.class);
            ShopCommand shopCommand = context.getBean(ShopCommand.class);
            InventoryCommand inventoryCommand = context.getBean(InventoryCommand.class);
            UseCommand useCommand = context.getBean(UseCommand.class);

            JDABuilder.createDefault(token)
                    .addEventListeners(leaderboardCommand, profileCommand, slotsCommand, workCommand, shopCommand, inventoryCommand, useCommand)
                    .setActivity(Activity.playing("OnibiExchange"))
                    .build()
                    .updateCommands()
                    .addCommands(
                            Commands.slash("profile", "Show your OnibiExchange profile"),
                            Commands.slash("work", "Earn Onicoins with cooldown"),
                            Commands.slash("leaderboard", "Show the leaderboard of the server"),
                            Commands.slash("slots", "GAMBLING !!!")
                                    .addOption(OptionType.INTEGER, "bet", "The amount of Onicoins you want to bet", true),
                            Commands.slash("shop", "View or buy items from the shop")
                                    .addOption(OptionType.STRING, "item", "The item you want to buy", false, true)
                                    .addOption(OptionType.INTEGER, "quantity", "How many to buy", false),
                            Commands.slash("inventory", "Show your owned items"),
                            Commands.slash("use", "Use an item from your inventory")
                    )
                    .queue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(0);
        }
    }

}
