package com.example.nasa.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 100, message = "Tên phải từ 2-100 ký tự")
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @NotNull(message = "Vui lòng chọn đánh giá")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 10, max = 1000, message = "Bình luận phải từ 10-1000 ký tự")
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "comment_date", nullable = false)
    private LocalDate commentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "image_path")
    private String imagePath;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Like> likes = new HashSet<>();

    // Constructors
    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.commentDate = LocalDate.now();
    }

    public Comment(String authorName, Integer rating, String commentText) {
        this();
        this.authorName = authorName;
        this.rating = rating;
        this.commentText = commentText;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDate getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(LocalDate commentDate) {
        this.commentDate = commentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    public void addLike(Like like) {
        likes.add(like);
        like.setComment(this);
    }

    public void removeLike(Like like) {
        likes.remove(like);
        like.setComment(null);
    }
}
