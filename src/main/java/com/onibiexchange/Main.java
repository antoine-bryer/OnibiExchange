package com.onibiexchange;

import com.onibiexchange.commands.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {

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

            JDABuilder.createDefault(args[1])
                    .addEventListeners(new ProfileCommand(), new WorkCommand(), new LeaderboardCommand(), new SlotsCommand())
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
                            /*Commands.slash("transfer", "Send ghost coins to another player")
                                    .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "target", "The user to send to", true)
                                    .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "amount", "Amount of coins to send", true)*/
                    )
                    .queue();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("", options);
            System.exit(0);
        }
    }
}
