package com.onibiexchange.repository;

import com.onibiexchange.model.RandomEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomEventRepository extends JpaRepository<RandomEvent, Long> {

    @Query("SELECT e FROM RandomEvent e ORDER BY random() LIMIT 1")
    public RandomEvent pickRandomEvent();

}
