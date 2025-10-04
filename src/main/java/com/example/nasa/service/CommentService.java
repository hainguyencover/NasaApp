package com.example.nasa.service;

import com.example.nasa.model.Comment;
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
}
