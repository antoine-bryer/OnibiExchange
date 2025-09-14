package com.onibiexchange.repository;

import com.onibiexchange.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class UserRepository {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("OnibiExchangePU");

    public User findByDiscordId(String discordId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.discordId = :discordId", User.class)
                    .setParameter("discordId", discordId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User merged = em.merge(user);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<User> getLeaderboard(){
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER by u.balance DESC LIMIT 10", User.class)
                    .getResultStream()
                    .toList();
        } finally {
            em.close();
        }
    }

}
