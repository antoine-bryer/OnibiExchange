package com.onibiexchange.service.impl;

import com.onibiexchange.model.User;
import com.onibiexchange.service.ILevelService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class LevelServiceImpl implements ILevelService {

    @Override
    public void addXp(User user, int xp){
        user.setXp(user.getXp() + xp);

        // Check if the user can level up
        while(user.getXp() >= xpToNextLevel(user.getLevel())){
            user.setXp(user.getXp() - xpToNextLevel(user.getLevel()));
            user.setLevel(user.getLevel() + 1);
        }
    }

    @Override
    public int xpToNextLevel(int level){
        return (level * 100) + (level * level * 20);
    }

}
