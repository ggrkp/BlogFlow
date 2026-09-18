package com.ggeorg.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

public class Transactional {

    private static final Logger LOGGER = LogManager.getLogger(Transactional.class);

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("blogflow");

    public static void execute(Consumer<EntityManager> action) {
        EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        try {
            LOGGER.info("Executing transaction.");
            entityManager.getTransaction().begin();
            action.accept(entityManager);
            entityManager.getTransaction().commit();
            LOGGER.info("Transaction executed.");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            LOGGER.error("Error executing transaction on thread: {} ", Thread.currentThread().getName(), e);
        } finally {
            entityManager.close();
        }
    }

    public static <R> R execute(Function<EntityManager, R> action) {
        EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        try {
            LOGGER.info("Executing transaction.");
            entityManager.getTransaction().begin();
            R result = action.apply(entityManager);
            entityManager.getTransaction().commit();
            LOGGER.info("Transaction executed successfully.");
            return result;
        } catch (Exception e) {
            LOGGER.error("Error executing transaction on thread: {}",
                    Thread.currentThread().getName(), e);
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Transaction failed", e);  // Re-throw!
        } finally {
            entityManager.close();
        }
    }

    public static EntityManager newEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    public static void closeFactory() {
        if (ENTITY_MANAGER_FACTORY != null && ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }

}
