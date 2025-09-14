package com.onibiexchange.repository;

import com.onibiexchange.models.RandomEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class RandomEventRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("OnibiExchangePU");

    public RandomEvent pickRandomEvent() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM RandomEvent e ORDER BY random() LIMIT 1", RandomEvent.class)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
}
