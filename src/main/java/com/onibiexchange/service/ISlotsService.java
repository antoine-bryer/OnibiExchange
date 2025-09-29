package com.onibiexchange.service;

import com.onibiexchange.model.Slots;

public interface ISlotsService {
    Slots getSlotsValues();
    void updateJackpot(Slots s, int amount);
    void resetJackpot(Slots s);
}
