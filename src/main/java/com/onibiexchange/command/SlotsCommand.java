package com.onibiexchange.command;

import com.onibiexchange.model.Slots;
import com.onibiexchange.model.User;
import com.onibiexchange.service.impl.SlotsServiceImpl;
import com.onibiexchange.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SlotsCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final SlotsServiceImpl slotsService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("slots")) {
            try{
                // Get or create user
                User user = userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());

                int bet = event.getOption("bet").getAsInt();

                // Check bet (1 - 100000)
                if(bet < 1 || bet > 100000){
                    event.reply("Bet a reasonable amount (1 - 100000)").setEphemeral(true).queue();
                }

                // Check balance
                if(bet > user.getBalance()){
                    event.reply("Get enough Onicoins to bet !").setEphemeral(true).queue();
                }

                // Generate slots result
                List<Emoji> slots = Arrays.asList(Emoji.fromUnicode("\uD83C\uDF47"),
                        Emoji.fromUnicode("\uD83C\uDF4A"),
                        Emoji.fromUnicode("\uD83C\uDF4B"),
                        Emoji.fromUnicode("\uD83C\uDF4C"),
                        Emoji.fromCustom("kosenoBaguette", Long.parseLong("1201881653564149812"), false));

                Map<Integer, List<Emoji>> slotsResult = new HashMap<>();
                List<Emoji> emojiLineResult = new ArrayList<>();
                for(int i = 0 ; i < 3 ; i++){
                    List<Emoji> emojiCol = slots.stream().collect(Collectors.collectingAndThen(Collectors.toList(), l -> {Collections.shuffle(l); return l.subList(0, 3);}));
                    slotsResult.put(i, emojiCol);
                    emojiLineResult.add(emojiCol.get(1));
                }

                // Update user balance
                String resultString = "";
                Slots s = slotsService.getSlotsValues();
                int currentJackpot = s.getJackpot();
                if(emojiLineResult.stream().map(Emoji::getName).distinct().count() == 1){
                    if(emojiLineResult.get(0).getName().equalsIgnoreCase("kosenoBaguette")){
                        // JACKPOT
                        resultString = "**YOU WIN THE JACKPOT !!! " + s.getJackpot() + " ONICOINS !!!**";
                        userService.updateBalance(user, s.getJackpot() + (bet * 2));
                        slotsService.resetJackpot(s);
                    } else {
                        // WIN
                        userService.updateBalance(user, bet * 2);
                        resultString = "**YOU WIN " + bet * 2 + " !!!**";
                    }
                } else {
                    // LOSE
                    userService.updateBalance(user, Math.negateExact(bet));
                    resultString = "**YOU LOSE...**";
                    slotsService.updateJackpot(s, bet);
                }

                // Generate Message
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("GAMBLING");
                eb.setAuthor(user.getUsername(), null, event.getUser().getAvatarUrl());
                eb.setDescription("JACKPOT : "+currentJackpot+" ONICOINS");

                StringBuilder reply = new StringBuilder();
                reply.append("**-------------------**").append('\n');
                StringBuilder line1 = new StringBuilder();
                StringBuilder line2 = new StringBuilder();
                StringBuilder line3 = new StringBuilder();
                for(int i = 0 ; i < 3 ; i++){
                    line1.append(slotsResult.get(i).get(0).getFormatted());
                    line2.append(slotsResult.get(i).get(1).getFormatted());
                    line3.append(slotsResult.get(i).get(2).getFormatted());

                    if(i != 2){
                        line1.append("\u00A0\u00A0\u00A0\u00A0").append("**:**").append("\u00A0\u00A0\u00A0\u00A0");
                        line2.append("\u00A0\u00A0\u00A0\u00A0").append("**:**").append("\u00A0\u00A0\u00A0\u00A0");
                        line3.append("\u00A0\u00A0\u00A0\u00A0").append("**:**").append("\u00A0\u00A0\u00A0\u00A0");
                    }

                    if(i == 2){
                        line2.append("\u00A0\u00A0\u00A0").append("**<**");
                    }
                }
                reply.append(line1).append('\n');
                reply.append(line2).append('\n');
                reply.append(line3).append('\n');
                reply.append("**-------------------**").append('\n');
                reply.append(resultString);

                eb.addField("You're playing Onislots by Scamsen inc. | bet = "+bet+" Onicoins", reply.toString(), false);

                event.replyEmbeds(eb.build()).queue();
            } catch (NullPointerException npe){
                event.reply("Place a bet to play").setEphemeral(true).queue();
            }
        }
    }
}
