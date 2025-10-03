package com.onibiexchange.service;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.LocalDate;

public interface ILotteryService {
    LocalDate getCurrentDrawDate();
    String enterBet(String discordUserId, int chosenNumber, int amount);
    String getStatus();
    EmbedBuilder runWeeklyDraw();

}
