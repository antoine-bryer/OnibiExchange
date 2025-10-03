package com.onibiexchange.service.impl;

import com.onibiexchange.model.LotteryBet;
import com.onibiexchange.model.LotteryDraw;
import com.onibiexchange.model.User;
import com.onibiexchange.repository.LotteryBetRepository;
import com.onibiexchange.repository.LotteryDrawRepository;
import com.onibiexchange.repository.UserRepository;
import com.onibiexchange.service.ILotteryService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.security.SecureRandom;
import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements ILotteryService {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 10;
    private static final long MIN_BET = 100L;     // minimum bet
    private static final ZoneId SERVER_ZONE = ZoneId.systemDefault();

    private final LotteryBetRepository betRepo;
    private final LotteryDrawRepository drawRepo;
    private final UserRepository userRepo;
    private final SecureRandom rng = new SecureRandom();

    /**
     * Calculates the date of the next draw (Saturday at 00:00) and returns the LocalDate for Saturday (date part only).
     * Example: if today is Monday/Thursday, returns the Saturday of the current week; if today is Saturday after 00:00,
     * it corresponds to today's draw (until next Saturday).
     */
    @Override
    public LocalDate getCurrentDrawDate() {
        ZonedDateTime now = ZonedDateTime.now(SERVER_ZONE);
        // Find the next Saturday 00:00 of the current week (or the current Saturday if before 00:00 on the same day).
        // Simplification: we identify the Saturday of the 'current' week as the draw key.
        DayOfWeek target = DayOfWeek.SATURDAY;
        int diff = target.getValue() - now.getDayOfWeek().getValue();
        if (diff < 0) diff += 7;
        return now.plusDays(diff).toLocalDate();
    }

    @Override
    @Transactional
    public String enterBet(String discordUserId, int chosenNumber, int amount) {
        if (chosenNumber < MIN_NUMBER || chosenNumber > MAX_NUMBER) {
            return "❌ Number must be between " + MIN_NUMBER + " and " + MAX_NUMBER + ".";
        }
        if (amount < MIN_BET) {
            return "❌ Minimum bet is " + MIN_BET + " coins.";
        }

        // User retrieval
        Optional<User> optUser = userRepo.findByDiscordId(discordUserId);
        if (optUser.isEmpty()) {
            return "❌ User not found in database.";
        }
        User user = optUser.get();

        if (user.getBalance() < amount) {
            return "💸 Not enough balance to place this bet.";
        }

        LocalDate drawDate = getCurrentDrawDate();

        // Check only one bet per user for this draw
        if (betRepo.findByUserAndDrawDate(user, drawDate).isPresent()) {
            return "⚠️ You already placed a bet for this week's draw.";
        }

        // Reserve the bet amount
        user.setBalance(user.getBalance() - amount);
        userRepo.save(user);

        // Enregistrer le pari
        LotteryBet bet = LotteryBet.builder()
                .user(user)
                .drawDate(drawDate)
                .chosenNumber(chosenNumber)
                .amount(amount)
                .build();
        betRepo.save(bet);

        // Créer/mettre à jour l'agrégat de pot pour ce tirage
        LotteryDraw draw = drawRepo.findByDrawDate(drawDate)
                .orElseGet(() -> LotteryDraw.builder()
                        .drawDate(drawDate)
                        .totalPot(0)
                        .distributed(false)
                        .build());
        draw.setTotalPot(draw.getTotalPot() + amount);
        drawRepo.save(draw);

        return "✅ Bet placed for draw on **" + drawDate + "** : number **" + chosenNumber + "** with **" + amount + " Onicoins**.";
    }

    @Override
    @Transactional(readOnly = true)
    public String getStatus() {
        LocalDate drawDate = getCurrentDrawDate();
        long pot = drawRepo.findByDrawDate(drawDate).map(LotteryDraw::getTotalPot).orElse(0L);
        int participants = betRepo.findAllByDrawDate(drawDate).size();
        return "🎰 Weekly lottery — Draw on **" + drawDate + "**\n" +
                "• Pot: **" + pot + "💰**\n" +
                "• Participants: **" + participants + "**\n" +
                "• Pick a number with `/lottery enter number:<1-10> amount:<coins>`";
    }

    /**
     * Official draw — to be run on Saturday 00:00 (server time).
     * Proportional distribution to winners' bets:
     * payout_i = totalPot * (bet_i / sum(winning_bets))
     */
    @Override
    @Transactional
    public EmbedBuilder runWeeklyDraw() {
        LocalDate drawDate = getCurrentDrawDate();
        int winning = rng.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;

        // Load or create the draw record
        LotteryDraw draw = drawRepo.findByDrawDate(drawDate)
                .orElseGet(() -> LotteryDraw.builder()
                        .drawDate(drawDate)
                        .totalPot(0)
                        .distributed(false)
                        .build());

        EmbedBuilder embed = new EmbedBuilder();

        // Title and color
        embed.setTitle("🎲 Lottery Draw Result")
                .setColor(Color.MAGENTA)
                .setFooter("Scamsen Inc. Lottery • Good luck next time!");

        // Date and winning number
        embed.addField("Draw Date", String.valueOf(drawDate), true);

        // Placeholder GIF
        embed.setImage("https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExNWljeDRjdWFxdTlwMmx2YmVwdDBuc3drMHg2a2t3czNzYjlyaXowZiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l2Je4c5M1xHUWaPzG/giphy.gif"); // replace with your GIF

        if (draw.isDistributed()) {
            embed.addField("Error", "⚠️ Draw for " + drawDate + " already processed.", false);
        } else {
            embed.addField("Winning Number", String.valueOf(winning), true);
        }

        List<LotteryBet> allBets = betRepo.findAllByDrawDate(drawDate);
        if (allBets.isEmpty()) {
            draw.setWinningNumber(null);
            draw.setDistributed(true);
            drawRepo.save(draw);
            embed.addField("Where are the gamblers ?", "🕰️ No bets for draw " + drawDate + ". Nothing to distribute.", false);
        }

        List<LotteryBet> winners = betRepo.findAllByDrawDateAndChosenNumber(drawDate, winning);
        int totalPot = Math.toIntExact(draw.getTotalPot());

        // Sum of winners' bets
        long sumWinnersBets = winners.stream().mapToLong(LotteryBet::getAmount).sum();

        // Proportional distribution
        // Winners' balances are credited in proportion to their bet.
        StringBuilder winnerList = new StringBuilder();

        if (winners.isEmpty()) {
            embed.addField("Winners", "No winners this draw. Pot cleared!", false);
        } else {
            for (LotteryBet b : winners) {
                double ratio = (double) b.getAmount() / (double) sumWinnersBets;
                int payout = Math.toIntExact(Math.round(totalPot * ratio)); // rounded to the nearest integer
                User u = b.getUser();
                u.setBalance(u.getBalance() + payout);
                userRepo.save(u);

                winnerList.append(b.getUser().getUsername()).append(" — ").append(payout).append(" Onicoins\n");
            }
            embed.addField("Winners", winnerList.toString(), false);
        }

        // Mark as distributed and reset pot to 0 after distribution
        draw.setWinningNumber(winning);
        draw.setDistributed(true);
        draw.setTotalPot(0);
        drawRepo.save(draw);

        return embed;
    }
}
