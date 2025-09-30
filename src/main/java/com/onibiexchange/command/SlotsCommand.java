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

import java.awt.*;
import java.util.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotsCommand extends ListenerAdapter {

    private final UserServiceImpl userService;
    private final SlotsServiceImpl slotsService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("slots")) return;

        try {
            User user = userService.getOrCreateUser(event.getUser().getId(), event.getUser().getName());
            int bet = getBetAmount(event);
            if (bet == -1) return;

            if (!hasSufficientBalance(event, user, bet)) return;

            List<Emoji> slots = generateSlotSymbols();
            Map<Integer, List<Emoji>> slotsResult = generateSlotGrid(slots);
            List<Emoji> middleLine = extractMiddleLine(slotsResult);

            Slots slotState = slotsService.getSlotsValues();
            int jackpotBefore = slotState.getJackpot();

            String resultMessage = applySlotOutcome(user, bet, middleLine, slotState);

            EmbedBuilder eb = buildSlotEmbed(event, user, jackpotBefore);
            eb.addField(formatTitle(bet), formatGrid(slotsResult, resultMessage), false);

            event.replyEmbeds(eb.build()).queue();

        } catch (NullPointerException e) {
            event.reply("Place a bet to play").setEphemeral(true).queue();
        }
    }

    private int getBetAmount(SlashCommandInteractionEvent event) {
        int bet = Objects.requireNonNull(event.getOption("bet")).getAsInt();
        if (bet < 1 || bet > 100000) {
            event.reply("Bet a reasonable amount (1 - 100000)").setEphemeral(true).queue();
            return -1;
        }
        return bet;
    }

    private boolean hasSufficientBalance(SlashCommandInteractionEvent event, User user, int bet) {
        if (bet > user.getBalance()) {
            event.reply("Get enough Onicoins to bet !").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private List<Emoji> generateSlotSymbols() {
        return Arrays.asList(
                Emoji.fromUnicode("\uD83C\uDF47"),
                Emoji.fromUnicode("\uD83C\uDF4A"),
                Emoji.fromUnicode("\uD83C\uDF4B"),
                Emoji.fromUnicode("\uD83C\uDF4C"),
                Emoji.fromCustom("kosenoBaguette", 1201881653564149812L, false)
        );
    }

    private Map<Integer, List<Emoji>> generateSlotGrid(List<Emoji> slots) {
        Map<Integer, List<Emoji>> grid = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            List<Emoji> column = new ArrayList<>(slots);
            Collections.shuffle(column);
            grid.put(i, column.subList(0, 3));
        }
        return grid;
    }

    private List<Emoji> extractMiddleLine(Map<Integer, List<Emoji>> grid) {
        return grid.values().stream()
                .map(col -> col.get(1))
                .toList();
    }

    private String applySlotOutcome(User user, int bet, List<Emoji> middleLine, Slots slotState) {
        boolean allSame = middleLine.stream().map(Emoji::getName).distinct().count() == 1;
        boolean isJackpot = allSame && middleLine.get(0).getName().equalsIgnoreCase("kosenoBaguette");

        if (isJackpot) {
            userService.updateBalance(user, slotState.getJackpot() + (bet * 2));
            slotsService.resetJackpot(slotState);
            return "**YOU WIN THE JACKPOT !!! " + slotState.getJackpot() + " ONICOINS !!!**";
        } else if (allSame) {
            userService.updateBalance(user, bet * 2);
            return "**YOU WIN " + bet * 2 + " !!!**";
        } else {
            userService.updateBalance(user, -bet);
            slotsService.updateJackpot(slotState, bet);
            return "**YOU LOSE...**";
        }
    }

    private EmbedBuilder buildSlotEmbed(SlashCommandInteractionEvent event, User user, int jackpot) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("GAMBLING");
        eb.setAuthor(user.getUsername(), null, event.getUser().getAvatarUrl());
        eb.setDescription("JACKPOT : " + jackpot + " ONICOINS");
        eb.setColor(Color.GREEN);
        return eb;
    }

    private String formatTitle(int bet) {
        return "You're playing Onislots by Scamsen inc. | bet = " + bet + " Onicoins";
    }

    private String formatGrid(Map<Integer, List<Emoji>> grid, String resultMessage) {
        StringBuilder b = new StringBuilder("**-------------------**\n");
        StringBuilder l1 = new StringBuilder();
        StringBuilder l2 = new StringBuilder();
        StringBuilder l3 = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            List<Emoji> col = grid.get(i);
            appendWithSeparator(l1, col.get(0).getFormatted(), i);
            appendWithSeparator(l2, col.get(1).getFormatted(), i);
            appendWithSeparator(l3, col.get(2).getFormatted(), i);

            if (i == 2) l2.append("\u00A0\u00A0\u00A0**<**");
        }
        b.append(l1).append('\n').append(l2).append('\n').append(l3).append('\n');
        b.append("**-------------------**\n").append(resultMessage);
        return b.toString();
    }

    private void appendWithSeparator(StringBuilder builder, String emoji, int index) {
        builder.append(emoji);
        if (index != 2) builder.append("\u00A0\u00A0\u00A0\u00A0**:**\u00A0\u00A0\u00A0\u00A0");
    }
}
