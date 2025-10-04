package com.example.nasa.service;

import com.example.nasa.model.Comment;
import com.example.nasa.model.Like;
import com.example.nasa.repository.CommentRepository;
import com.example.nasa.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    public boolean toggleLike(Long commentId, String userIp) {
        if (likeRepository.existsByCommentIdAndUserIp(commentId, userIp)) {
            // Unlike
            likeRepository.deleteByCommentIdAndUserIp(commentId, userIp);
            return false;
        } else {
            // Like
            Comment comment = commentRepository.findById(commentId);
            if (comment != null) {
                Like like = new Like(comment, userIp);
                likeRepository.save(like);
                return true;
            }
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long commentId, String userIp) {
        return likeRepository.existsByCommentIdAndUserIp(commentId, userIp);
    }
}
