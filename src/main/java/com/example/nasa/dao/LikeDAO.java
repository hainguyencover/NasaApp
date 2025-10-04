package com.example.nasa.dao;

import com.example.nasa.model.Like;
import com.example.nasa.util.HibernateUtil;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class LikeDAO {
    public void saveLike(Like like) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(like);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public boolean hashUserLikeComment(Long commentId, String userIp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(l) FROM Like l WHERE l.comment.id = :commentId AND l.userIp = :userIp";
            Query query = session.createQuery(hql);
            query.setParameter("commentId", commentId);
            query.setParameter("userIp", userIp);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeLike(Long commentId, String userIp) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "DELETE FROM Like l WHERE l.comment.id = :commentId AND l.userIp = :userIp";
            Query query = session.createQuery(hql);
            query.setParameter("commentId", commentId);
            query.setParameter("userIp", userIp);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
