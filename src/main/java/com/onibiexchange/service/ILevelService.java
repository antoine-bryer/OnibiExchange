package com.onibiexchange.service;

import com.onibiexchange.model.User;

public interface ILevelService {

    public void addXp(User user, int xp);

    public int xpToNextLevel(int level);

}
