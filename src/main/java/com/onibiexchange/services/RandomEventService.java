package com.onibiexchange.services;

import com.onibiexchange.models.RandomEvent;
import com.onibiexchange.repository.RandomEventRepository;

import java.util.Random;

public class RandomEventService {
    private final RandomEventRepository repository = new RandomEventRepository();
    private final Random random = new Random();

    public RandomEvent getRandomEvent() {
        RandomEvent event = repository.pickRandomEvent();

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
