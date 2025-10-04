package com.example.nasa.repository;

import com.example.nasa.model.Like;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LikeRepository {

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(Like like) {
        getSession().save(like);
    }

    public void delete(Like like) {
        getSession().delete(like);
    }

    public Like findById(Long id) {
        return getSession().get(Like.class, id);
    }

    public boolean existsByCommentIdAndUserIp(Long commentId, String userIp) {
        String hql = "SELECT COUNT(l) FROM Like l WHERE l.comment.id = :commentId AND l.userIp = :userIp";
        Query<Long> query = getSession().createQuery(hql, Long.class);
        query.setParameter("commentId", commentId);
        query.setParameter("userIp", userIp);
        Long count = query.uniqueResult();
        return count != null && count > 0;
    }

    public Like findByCommentIdAndUserIp(Long commentId, String userIp) {
        String hql = "FROM Like l WHERE l.comment.id = :commentId AND l.userIp = :userIp";
        Query<Like> query = getSession().createQuery(hql, Like.class);
        query.setParameter("commentId", commentId);
        query.setParameter("userIp", userIp);
        return query.uniqueResult();
    }

    public int deleteByCommentIdAndUserIp(Long commentId, String userIp) {
        String hql = "DELETE FROM Like l WHERE l.comment.id = :commentId AND l.userIp = :userIp";
        Query query = getSession().createQuery(hql);
        query.setParameter("commentId", commentId);
        query.setParameter("userIp", userIp);
        return query.executeUpdate();
    }
}
