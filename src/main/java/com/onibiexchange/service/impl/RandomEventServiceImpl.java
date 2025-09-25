package com.onibiexchange.service.impl;

import com.onibiexchange.model.RandomEvent;
import com.onibiexchange.repository.RandomEventRepository;
import com.onibiexchange.service.IRandomEventService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Transactional
public class RandomEventServiceImpl implements IRandomEventService {

    private final Random random = new Random();

    @Autowired
    private RandomEventRepository randomEventRepository;

    @Override
    public RandomEvent getRandomEvent() {
        RandomEvent event = randomEventRepository.pickRandomEvent();
        if (event != null) {
           int rarity = event.getRarity();
           Random random = new Random();

           if(random.nextInt(100) < rarity){
               return event;
           }
        }
        return null;
    }
}
