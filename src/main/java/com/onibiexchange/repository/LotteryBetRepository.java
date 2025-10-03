package com.onibiexchange.repository;

import com.onibiexchange.model.LotteryBet;
import com.onibiexchange.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LotteryBetRepository extends JpaRepository<LotteryBet, Long> {
    Optional<LotteryBet> findByUserAndDrawDate(User user, LocalDate drawDate);
    List<LotteryBet> findAllByDrawDate(LocalDate drawDate);
    List<LotteryBet> findAllByDrawDateAndChosenNumber(LocalDate drawDate, int chosenNumber);
}
