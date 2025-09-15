package com.onibiexchange.services;

import com.onibiexchange.models.User;

public class LevelService {

    public void addXp(User user, int xp){
        user.setXp(user.getXp() + xp);

        // Check if the user can level up
        while(user.getXp() >= xpToNextLevel(user.getLevel())){
            user.setXp(user.getXp() - xpToNextLevel(user.getLevel()));
            user.setLevel(user.getLevel() + 1);
        }
    }

    public int xpToNextLevel(int level){
        return (level * 100) + (level * level * 20);
    }

}
