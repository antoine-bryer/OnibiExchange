package com.onibiexchange.scheduler;

import com.onibiexchange.Main;
import com.onibiexchange.service.impl.LotteryServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotteryScheduler {
    private final LotteryServiceImpl lotteryService;

    @Scheduled(cron = "0 0 0 ? * SAT")
    public void weeklyDraw() {
        EmbedBuilder result = lotteryService.runWeeklyDraw();
        Main.getJDA().getTextChannelById(System.getenv("LOTTERY_CHANNEL_ID"))
                .sendMessageEmbeds(result.build())
                .queue();
    }
}
