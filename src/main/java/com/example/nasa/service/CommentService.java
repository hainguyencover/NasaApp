package com.example.nasa.service;

import com.example.nasa.model.Comment;
import com.example.nasa.model.Page;
import com.example.nasa.model.Pageable;
import com.example.nasa.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void updateComment(Comment comment) {
        commentRepository.update(comment);
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id);
        if (comment != null) {
            commentRepository.delete(comment);
        }
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByDate(LocalDate date) {
        return commentRepository.findByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Comment> getTodayComments() {
        return commentRepository.findTodayComments();
    }

    @Transactional(readOnly = true)
    public Long countCommentsByDate(LocalDate date) {
        return commentRepository.countByDate(date);
    }

    // ============== PAGINATION METHODS ==============

    /**
     * Get all comments với pagination
     */
    @Transactional(readOnly = true)
    public Page<Comment> getAllCommentsWithPagination(Pageable pageable) {
        return commentRepository.findAllWithPagination(pageable);
    }

    /**
     * Get comments by date với pagination
     */
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByDateWithPagination(LocalDate date, Pageable pageable) {
        return commentRepository.findByDateWithPagination(date, pageable);
    }

    /**
     * Get today's comments với pagination
     */
    @Transactional(readOnly = true)
    public Page<Comment> getTodayCommentsWithPagination(Pageable pageable) {
        return commentRepository.findTodayCommentsWithPagination(pageable);
    }

    /**
     * Get comments sorted by likes với pagination
     */
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsSortedByLikes(Pageable pageable) {
        return commentRepository.findAllSortedByLikes(pageable);
    }

    // ============== SEARCH METHODS ==============

    /**
     * Search comments với pagination
     */
    @Transactional(readOnly = true)
    public Page<Comment> searchCommentsWithPagination(String searchTerm, Pageable pageable) {
        return commentRepository.searchComments(searchTerm, pageable);
    }

    /**
     * Get search suggestions for autocomplete
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String searchTerm, int limit) {
        return commentRepository.getSearchSuggestions(searchTerm, limit);
    }

    /**
     * Count all comments
     */
    @Transactional(readOnly = true)
    public Long countAllComments() {
        return commentRepository.countAll();
    }

    /**
     * Get top rated comment
     */
    @Transactional(readOnly = true)
    public Comment getTopRatedComment() {
        return commentRepository.findTopRatedComment();
    }

    /**
     * Get most liked comment
     */
    @Transactional(readOnly = true)
    public Comment getMostLikedComment() {
        return commentRepository.findMostLikedComment();
    }
}
