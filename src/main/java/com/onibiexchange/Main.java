package com.onibiexchange;

import com.onibiexchange.command.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static JDA jda;

    public static void init(String token, String[] args) {
        if (jda == null) {
            ApplicationContext context = SpringApplication.run(Main.class, args);

            LeaderboardCommand leaderboardCommand = context.getBean(LeaderboardCommand.class);
            ProfileCommand profileCommand = context.getBean(ProfileCommand.class);
            SlotsCommand slotsCommand = context.getBean(SlotsCommand.class);
            WorkCommand workCommand = context.getBean(WorkCommand.class);
            ShopCommand shopCommand = context.getBean(ShopCommand.class);
            InventoryCommand inventoryCommand = context.getBean(InventoryCommand.class);
            UseCommand useCommand = context.getBean(UseCommand.class);
            LotteryCommand lotteryCommand = context.getBean(LotteryCommand.class);

            jda = JDABuilder.createDefault(token)
                    .addEventListeners(leaderboardCommand, profileCommand, slotsCommand, workCommand, shopCommand, inventoryCommand, useCommand, lotteryCommand)
                    .setActivity(Activity.playing("OnibiExchange"))
                    .build();
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void main(String[] args) {
        try {
            String token = System.getenv("DISCORD_TOKEN");

            if (token == null || token.isEmpty()) {
                logger.error("❌ ERROR: DISCORD_TOKEN environment variable is not set.");
                System.exit(1);
            }

            logger.info("✅ Starting OnibiExchange bot...");

            init(token, args);

            jda.updateCommands()
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
                        Commands.slash("use", "Use an item from your inventory"),
                        Commands.slash("lottery", "Weekly lottery")
                                .addSubcommands(
                                       new SubcommandData("enter", "Enter current weekly draw")
                                                .addOption(OptionType.INTEGER, "number", "Pick a number (1-10)", true)
                                                .addOption(OptionType.INTEGER, "amount", "Bet amount", true),
                                       new SubcommandData("status", "Show pot and participants")
                                )
                    )
                    .queue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(0);
        }
    }

}
