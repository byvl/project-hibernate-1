package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/application.properties"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        String nativeSql = "SELECT * FROM player LIMIT :pageSize OFFSET :offset";
        try (Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createNativeQuery(nativeSql, Player.class);
            int offset = pageNumber * pageSize;
            query.setParameter("offset", offset);
            query.setParameter("pageSize", pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("player_getAllCount", Long.class);
            return query.getSingleResult().intValue();
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(player);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Player playerMerge;// = null;
            try {
                playerMerge = (Player) session.merge(player);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
            return playerMerge;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player player = session.get(Player.class, id);
            return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.delete(player);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }

    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();

    }
}