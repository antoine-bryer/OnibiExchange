package com.onibiexchange.repository;

import com.onibiexchange.models.RandomEvent;
import com.onibiexchange.models.Slots;
import com.onibiexchange.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class SlotsRepository {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("OnibiExchangePU");

    public Slots getSlotsValues(){
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Slots e", Slots.class)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public Slots save(Slots slots) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Slots merged = em.merge(slots);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

}
