package com.onibiexchange.service.impl;

import com.onibiexchange.model.Slots;
import com.onibiexchange.repository.SlotsRepository;
import com.onibiexchange.service.ISlotsService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SlotsServiceImpl implements ISlotsService {

    private final SlotsRepository slotsRepository;

    public SlotsServiceImpl(SlotsRepository slotsRepository) {
        this.slotsRepository = slotsRepository;
    }

    @Override
    public Slots getSlotsValues(){
        return slotsRepository.findFirstBy();
    }

    @Override
    public void updateJackpot(Slots s, int amount){
        int newJackpot = s.getJackpot()+amount;
        s.setJackpot(newJackpot);
        slotsRepository.save(s);
    }

    @Override
    public void resetJackpot(Slots s){
        s.setJackpot(0);
        slotsRepository.save(s);
    }
}
