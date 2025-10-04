package com.example.nasa.repository;

import com.example.nasa.model.Comment;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class CommentRepository {

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(Comment comment) {
        getSession().save(comment);
    }

    public void update(Comment comment) {
        getSession().update(comment);
    }

    public void delete(Comment comment) {
        getSession().delete(comment);
    }

    public Comment findById(Long id) {
        return getSession().get(Comment.class, id);
    }

    public List<Comment> findAll() {
        String hql = "FROM Comment c ORDER BY c.createdAt DESC";
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        return query.list();
    }

    public List<Comment> findByDate(LocalDate date) {
        String hql = "FROM Comment c WHERE c.commentDate = :date ORDER BY c.createdAt DESC";
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setParameter("date", date);
        return query.list();
    }

    public List<Comment> findTodayComments() {
        return findByDate(LocalDate.now());
    }

    public Long countByDate(LocalDate date) {
        String hql = "SELECT COUNT(c) FROM Comment c WHERE c.commentDate = :date";
        Query<Long> query = getSession().createQuery(hql, Long.class);
        query.setParameter("date", date);
        return query.uniqueResult();
    }
}
