package com.onibiexchange.command;

import com.onibiexchange.model.User;
import com.onibiexchange.service.impl.UserServiceImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaderboardCommand extends ListenerAdapter {

    private final UserServiceImpl userService;

    public LeaderboardCommand(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("leaderboard")) {
            List<User> leaderboard = userService.getLeaderboard();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("LEADERBOARD");
            eb.setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl());
            eb.setDescription("Top 10 Onicoin stacker");

            if(leaderboard != null){
                StringBuilder leaderboardString = new StringBuilder();
                int place = 1;
                for(User u : leaderboard){
                    leaderboardString.append("- ").append(place).append(" : ").append(u.getUsername()).append(" (").append(u.getBalance()).append(")");
                    if(place != leaderboard.size()){
                        leaderboardString.append('\n');
                    }
                    place++;
                }
                eb.addField("", leaderboardString.toString(), false);
            }

            event.replyEmbeds(eb.build()).queue();
        }
    }

}
