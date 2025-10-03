package com.onibiexchange.repository;

import com.onibiexchange.model.LotteryDraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LotteryDrawRepository extends JpaRepository<LotteryDraw, Long> {
    Optional<LotteryDraw> findByDrawDate(LocalDate drawDate);
}
