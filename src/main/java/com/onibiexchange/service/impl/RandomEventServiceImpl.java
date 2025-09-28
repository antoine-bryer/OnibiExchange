package com.onibiexchange.service.impl;

import com.onibiexchange.model.RandomEvent;
import com.onibiexchange.repository.RandomEventRepository;
import com.onibiexchange.service.IRandomEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@RequiredArgsConstructor
@Service
@Transactional
public class RandomEventServiceImpl implements IRandomEventService {

    private final RandomEventRepository randomEventRepository;
    private final Random random = new Random();

    @Override
    public RandomEvent getRandomEvent() {
        RandomEvent event = randomEventRepository.pickRandomEvent();
        if (event != null) {
           int rarity = event.getRarity();
           if(random.nextInt(100) < rarity){
               return event;
           }
        }
        return null;
    }
}
