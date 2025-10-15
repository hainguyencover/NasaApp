package com.example.nasa.repository;

import com.example.nasa.model.Comment;
import com.example.nasa.model.Page;
import com.example.nasa.model.Pageable;
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

    // ============== PAGINATION METHODS ==============

    /**
     * Find comments với pagination
     */
    public Page<Comment> findAllWithPagination(Pageable pageable) {
        // Query để lấy dữ liệu
        String hql = "FROM Comment c ORDER BY c." + pageable.getSortBy() + " " + pageable.getDirection();
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getSize());
        List<Comment> content = query.list();

        // Query để đếm tổng số
        Long totalElements = countAll();

        return new Page<>(content, pageable.getPage(), pageable.getSize(), totalElements);
    }

    /**
     * Find comments by date với pagination
     */
    public Page<Comment> findByDateWithPagination(LocalDate date, Pageable pageable) {
        // Query để lấy dữ liệu
        String hql = "FROM Comment c WHERE c.commentDate = :date ORDER BY c." +
                pageable.getSortBy() + " " + pageable.getDirection();
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setParameter("date", date);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getSize());
        List<Comment> content = query.list();

        // Query để đếm tổng số
        Long totalElements = countByDate(date);

        return new Page<>(content, pageable.getPage(), pageable.getSize(), totalElements);
    }

    /**
     * Find today's comments với pagination
     */
    public Page<Comment> findTodayCommentsWithPagination(Pageable pageable) {
        return findByDateWithPagination(LocalDate.now(), pageable);
    }

    /**
     * Đếm tổng số comments
     */
    public Long countAll() {
        String hql = "SELECT COUNT(c) FROM Comment c";
        Query<Long> query = getSession().createQuery(hql, Long.class);
        return query.uniqueResult();
    }

    /**
     * Find comments được sort theo số lượng likes
     */
    public Page<Comment> findAllSortedByLikes(Pageable pageable) {
        String hql = "SELECT c FROM Comment c LEFT JOIN c.likes l " +
                "GROUP BY c ORDER BY COUNT(l) " + pageable.getDirection();
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getSize());
        List<Comment> content = query.list();

        Long totalElements = countAll();

        return new Page<>(content, pageable.getPage(), pageable.getSize(), totalElements);
    }

    // ============== SEARCH METHODS ==============

    /**
     * Search comments by author name or comment text với pagination
     */
    public Page<Comment> searchComments(String searchTerm, Pageable pageable) {
        String hql = "FROM Comment c WHERE " +
                "LOWER(c.authorName) LIKE LOWER(:searchTerm) OR " +
                "LOWER(c.commentText) LIKE LOWER(:searchTerm) " +
                "ORDER BY c." + pageable.getSortBy() + " " + pageable.getDirection();

        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getSize());
        List<Comment> content = query.list();

        // Count matching results
        Long totalElements = countSearchResults(searchTerm);

        return new Page<>(content, pageable.getPage(), pageable.getSize(), totalElements);
    }

    /**
     * Count search results
     */
    public Long countSearchResults(String searchTerm) {
        String hql = "SELECT COUNT(c) FROM Comment c WHERE " +
                "LOWER(c.authorName) LIKE LOWER(:searchTerm) OR " +
                "LOWER(c.commentText) LIKE LOWER(:searchTerm)";
        Query<Long> query = getSession().createQuery(hql, Long.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.uniqueResult();
    }

    /**
     * Get search suggestions (autocomplete)
     */
    public List<String> getSearchSuggestions(String searchTerm, int limit) {
        String hql = "SELECT DISTINCT c.authorName FROM Comment c WHERE " +
                "LOWER(c.authorName) LIKE LOWER(:searchTerm) " +
                "ORDER BY c.authorName";
        Query<String> query = getSession().createQuery(hql, String.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        query.setMaxResults(limit);
        return query.list();
    }

    /**
     * Get top rated comment
     */
    public Comment findTopRatedComment() {
        String hql = "FROM Comment c ORDER BY c.rating DESC, c.createdAt DESC";
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    /**
     * Get most liked comment
     */
    public Comment findMostLikedComment() {
        String hql = "SELECT c FROM Comment c LEFT JOIN c.likes l " +
                "GROUP BY c ORDER BY COUNT(l) DESC";
        Query<Comment> query = getSession().createQuery(hql, Comment.class);
        query.setMaxResults(1);
        return query.uniqueResult();
    }
}
