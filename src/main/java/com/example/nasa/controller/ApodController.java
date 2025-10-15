package com.example.nasa.controller;

import com.example.nasa.model.Comment;
import com.example.nasa.model.Page;
import com.example.nasa.model.Pageable;
import com.example.nasa.service.CommentService;
import com.example.nasa.service.FileStorageService;
import com.example.nasa.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/")
public class ApodController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    @Value("${nasa.api.url}")
    private String nasaApiUrl;

    // Home page - Display APOD and comments
    @GetMapping
    public ModelAndView index(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "filter", defaultValue = "today") String filter) {

        ModelAndView mav = new ModelAndView("index");

        // Create Pageable object
        Pageable pageable = new Pageable(page, size, sortBy, direction);

        // Get comments based on filter
        Page<Comment> commentPage;

        switch (filter) {
            case "all":
                // All comments
                if ("likes".equals(sortBy)) {
                    commentPage = commentService.getCommentsSortedByLikes(pageable);
                } else {
                    commentPage = commentService.getAllCommentsWithPagination(pageable);
                }
                break;

            case "date":
                // Comments by specific date
                if (date != null) {
                    commentPage = commentService.getCommentsByDateWithPagination(date, pageable);
                } else {
                    commentPage = commentService.getTodayCommentsWithPagination(pageable);
                }
                break;

            case "today":
            default:
                // Today's comments (default)
                commentPage = commentService.getTodayCommentsWithPagination(pageable);
                break;
        }

        // Create empty comment object for form binding
        Comment newComment = new Comment();

        // Add data to model
        mav.addObject("commentPage", commentPage);
        mav.addObject("comment", newComment);
        mav.addObject("nasaApiKey", nasaApiKey);
        mav.addObject("nasaApiUrl", nasaApiUrl);

        // Add pagination parameters for view
        mav.addObject("currentPage", page);
        mav.addObject("pageSize", size);
        mav.addObject("sortBy", sortBy);
        mav.addObject("direction", direction);
        mav.addObject("filter", filter);
        mav.addObject("selectedDate", date);

        return mav;
    }

    // Add new comment with ModelAttribute and form data binding
    @PostMapping("/comment/add")
    public String addComment(
            @Valid @ModelAttribute("comment") Comment comment,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "filter", defaultValue = "today") String filter,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check validation errors
        if (bindingResult.hasErrors()) {
            // Reload page with errors
            Pageable pageable = new Pageable(page, size, sortBy, direction);
            Page<Comment> commentPage;

            if ("all".equals(filter)) {
                commentPage = commentService.getAllCommentsWithPagination(pageable);
            } else {
                commentPage = commentService.getTodayCommentsWithPagination(pageable);
            }

            model.addAttribute("commentPage", commentPage);
            model.addAttribute("nasaApiKey", nasaApiKey);
            model.addAttribute("nasaApiUrl", nasaApiUrl);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("filter", filter);

            return "index";
        }

        try {
            // Handle file upload if present
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = fileStorageService.storeFile(imageFile);
                comment.setImagePath(filename);
            }

            // Save comment
            commentService.saveComment(comment);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đánh giá của bạn đã được gửi thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra khi gửi đánh giá: " + e.getMessage());
        }

        // Redirect với pagination parameters
        return String.format("redirect:/?page=%d&size=%d&sortBy=%s&direction=%s&filter=%s",
                page, size, sortBy, direction, filter);
    }

    // Toggle like using @RequestParam
    @PostMapping("/comment/like")
    public String toggleLike(
            @RequestParam("commentId") Long commentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "filter", defaultValue = "today") String filter,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            String userIp = getClientIP(request);
            boolean liked = likeService.toggleLike(commentId, userIp);

            if (liked) {
                redirectAttributes.addFlashAttribute("likeMessage", "Đã thích bình luận!");
            } else {
                redirectAttributes.addFlashAttribute("likeMessage", "Đã bỏ thích bình luận!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
        }

        // Redirect về cùng page
        return String.format("redirect:/?page=%d&size=%d&sortBy=%s&direction=%s&filter=%s",
                page, size, sortBy, direction, filter);
    }

    // Delete comment
    @PostMapping("/comment/delete/{id}")
    public String deleteComment(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "filter", defaultValue = "today") String filter,
            RedirectAttributes redirectAttributes) {

        try {
            Comment comment = commentService.getCommentById(id);

            // Delete associated image file if exists
            if (comment != null && comment.getImagePath() != null) {
                fileStorageService.deleteFile(comment.getImagePath());
            }

            commentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bình luận!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra khi xóa bình luận: " + e.getMessage());
        }

        return String.format("redirect:/?page=%d&size=%d&sortBy=%s&direction=%s&filter=%s",
                page, size, sortBy, direction, filter);
    }

    /**
     * View single comment detail
     */
    @GetMapping("/comment/{id}")
    public ModelAndView viewComment(@PathVariable("id") Long id) {
        ModelAndView mav = new ModelAndView("comment-detail");
        Comment comment = commentService.getCommentById(id);
        mav.addObject("comment", comment);
        return mav;
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
