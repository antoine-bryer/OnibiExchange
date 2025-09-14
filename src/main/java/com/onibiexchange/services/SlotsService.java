package com.onibiexchange.services;

import com.onibiexchange.models.Slots;
import com.onibiexchange.repository.SlotsRepository;

public class SlotsService {

    private final SlotsRepository slotsRepository = new SlotsRepository();

    public Slots getSlotsValues(){
        return slotsRepository.getSlotsValues();
    }

    public void updateJackpot(Slots s, int amount){
        int newJackpot = s.getJackpot()+amount;
        s.setJackpot(newJackpot);
        slotsRepository.save(s);
    }

    public void resetJackpot(Slots s){
        s.setJackpot(0);
        slotsRepository.save(s);
    }
}
