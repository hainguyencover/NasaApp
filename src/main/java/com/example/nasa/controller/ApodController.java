package com.example.nasa.controller;

import com.example.nasa.model.Comment;
import com.example.nasa.service.CommentService;
import com.example.nasa.service.FileStorageService;
import com.example.nasa.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");

        // Get today's comments
        List<Comment> comments = commentService.getTodayComments();

        // Create empty comment object for form binding
        Comment newComment = new Comment();

        mav.addObject("comments", comments);
        mav.addObject("comment", newComment);
        mav.addObject("nasaApiKey", nasaApiKey);
        mav.addObject("nasaApiUrl", nasaApiUrl);

        return mav;
    }

    // Add new comment with ModelAttribute and form data binding
    @PostMapping("/comment/add")
    public String addComment(
            @Valid @ModelAttribute("comment") Comment comment,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("comments", commentService.getTodayComments());
            model.addAttribute("nasaApiKey", nasaApiKey);
            model.addAttribute("nasaApiUrl", nasaApiUrl);
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

        return "redirect:/";
    }

    // Toggle like using @RequestParam
    @PostMapping("/comment/like")
    public String toggleLike(
            @RequestParam("commentId") Long commentId,
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

        return "redirect:/";
    }

    // Delete comment
    @PostMapping("/comment/delete/{id}")
    public String deleteComment(
            @PathVariable("id") Long id,
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

        return "redirect:/";
    }

    // View single comment detail
    @GetMapping("/comment/{id}")
    public ModelAndView viewComment(@PathVariable("id") Long id) {
        ModelAndView mav = new ModelAndView("comment-detail");
        Comment comment = commentService.getCommentById(id);
        mav.addObject("comment", comment);
        return mav;
    }

    // Get client IP address
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
