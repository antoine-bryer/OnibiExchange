package com.onibiexchange.service;

import com.onibiexchange.model.User;

public interface ILevelService {
    void addXp(User user, int xp);
    int xpToNextLevel(int level);
}
