package com.onibiexchange.repository;

import com.onibiexchange.model.Slots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotsRepository extends JpaRepository<Slots, Long> {

    public Slots findFirstBy();

}
