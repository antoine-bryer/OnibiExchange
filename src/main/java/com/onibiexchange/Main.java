package com.onibiexchange;

import com.onibiexchange.command.LeaderboardCommand;
import com.onibiexchange.command.ProfileCommand;
import com.onibiexchange.command.SlotsCommand;
import com.onibiexchange.command.WorkCommand;
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

            JDABuilder.createDefault(token)
                    .addEventListeners(leaderboardCommand, profileCommand, slotsCommand, workCommand)
                    .setActivity(Activity.playing("OnibiExchange"))
                    .build()
                    .updateCommands()
                    .addCommands(
                            //Commands.slash("balance", "Show your Onicoin balance"),
                            Commands.slash("profile", "Show your OnibiExchange profile"),
                            Commands.slash("work", "Earn Onicoins with cooldown"),
                            Commands.slash("leaderboard", "Show the leaderboard of the server"),
                            Commands.slash("slots", "GAMBLING !!!")
                                    .addOption(OptionType.INTEGER, "bet", "The amount of Onicoins you want to bet", true)
                    )
                    .queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

}
