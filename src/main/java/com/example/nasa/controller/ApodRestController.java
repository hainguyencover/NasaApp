package com.example.nasa.controller;

import com.example.nasa.model.Comment;
import com.example.nasa.model.Page;
import com.example.nasa.model.Pageable;
import com.example.nasa.service.CommentService;
import com.example.nasa.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for AJAX requests
 * Returns JSON responses for dynamic content loading
 */
@RestController
@RequestMapping("/api")
public class ApodRestController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    /**
     * Get paginated comments (AJAX)
     * Returns JSON with comments and pagination metadata
     */
    @GetMapping("/comments")
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "filter", defaultValue = "today") String filter,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "search", required = false) String search) {

        try {
            Pageable pageable = new Pageable(page, size, sortBy, direction);
            Page<Comment> commentPage;

            // Apply filters
            if (search != null && !search.trim().isEmpty()) {
                // Search functionality
                commentPage = commentService.searchCommentsWithPagination(search, pageable);
            } else {
                switch (filter) {
                    case "all":
                        if ("likes".equals(sortBy)) {
                            commentPage = commentService.getCommentsSortedByLikes(pageable);
                        } else {
                            commentPage = commentService.getAllCommentsWithPagination(pageable);
                        }
                        break;
                    case "date":
                        if (date != null) {
                            commentPage = commentService.getCommentsByDateWithPagination(date, pageable);
                        } else {
                            commentPage = commentService.getTodayCommentsWithPagination(pageable);
                        }
                        break;
                    case "today":
                    default:
                        commentPage = commentService.getTodayCommentsWithPagination(pageable);
                        break;
                }
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", commentPage);
            response.put("message", "Comments loaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading comments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Toggle like (AJAX)
     * Returns updated like count
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable("commentId") Long commentId,
            HttpServletRequest request) {

        try {
            String userIp = getClientIP(request);
            boolean liked = likeService.toggleLike(commentId, userIp);

            // Get updated comment to return new like count
            Comment comment = commentService.getCommentById(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("liked", liked);
            response.put("likeCount", comment.getLikeCount());
            response.put("message", liked ? "Liked!" : "Unliked!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error toggling like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete comment (AJAX)
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable("id") Long id) {

        try {
            Comment comment = commentService.getCommentById(id);

            if (comment == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Comment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            commentService.deleteComment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get comment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {

        try {
            Long totalComments = commentService.countAllComments();
            Long todayComments = commentService.countCommentsByDate(LocalDate.now());
            Comment topRatedComment = commentService.getTopRatedComment();
            Comment mostLikedComment = commentService.getMostLikedComment();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalComments", totalComments);
            stats.put("todayComments", todayComments);
            stats.put("topRatedComment", topRatedComment);
            stats.put("mostLikedComment", mostLikedComment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Search suggestions (autocomplete)
     */
    @GetMapping("/search/suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam("query") String query) {

        try {
            // Get top 5 matching author names and comment snippets
            List<String> suggestions = commentService.getSearchSuggestions(query, 5);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suggestions", suggestions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading suggestions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
