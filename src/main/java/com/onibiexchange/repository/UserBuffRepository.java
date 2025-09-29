package com.onibiexchange.repository;

import com.onibiexchange.model.User;
import com.onibiexchange.model.UserBuff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBuffRepository extends JpaRepository<UserBuff, Long> {
    List<UserBuff> findByUser(User user);
    List<UserBuff> findByUserAndEffectType(User user, String effectType);
    public boolean existsByUserAndEffectType(User user, String effectType);
    public List<UserBuff> findByUserAndExpirationDateBefore(User user, LocalDateTime dateTime);
}
