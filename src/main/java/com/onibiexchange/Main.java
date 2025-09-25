package com.onibiexchange;

import com.onibiexchange.command.LeaderboardCommand;
import com.onibiexchange.command.ProfileCommand;
import com.onibiexchange.command.SlotsCommand;
import com.onibiexchange.command.WorkCommand;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        Options options = new Options();
        options.addOption(new Option("t", "token", true, "Provide the token during startup."));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            // Check if the token argument was provided and has a value. If it doesn't, return null.
            String token = cmd.hasOption("token") ? cmd.getOptionValue("token") : null;
            if (token == null) {
                System.out.println("ERROR: No token provided, please provide a token using the -t or --token flag.");
                formatter.printHelp("", options);
                System.exit(0);
            }

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
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("", options);
            System.exit(0);
        }
    }

}
