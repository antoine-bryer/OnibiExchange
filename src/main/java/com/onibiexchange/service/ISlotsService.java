package com.onibiexchange.service;

import com.onibiexchange.model.Slots;

public interface ISlotsService {

    public Slots getSlotsValues();

    public void updateJackpot(Slots s, int amount);

    public void resetJackpot(Slots s);

}
