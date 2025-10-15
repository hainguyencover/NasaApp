package com.example.nasa.model;

public class Pageable {
    private int page;           // Số trang (bắt đầu từ 0)
    private int size;           // Số items mỗi trang
    private String sortBy;      // Field để sort
    private String direction;   // ASC hoặc DESC

    // Default values
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "createdAt";
    private static final String DEFAULT_DIRECTION = "DESC";

    // Constructors
    public Pageable() {
        this.page = DEFAULT_PAGE;
        this.size = DEFAULT_SIZE;
        this.sortBy = DEFAULT_SORT;
        this.direction = DEFAULT_DIRECTION;
    }

    public Pageable(int page, int size) {
        this.page = page;
        this.size = size;
        this.sortBy = DEFAULT_SORT;
        this.direction = DEFAULT_DIRECTION;
    }

    public Pageable(int page, int size, String sortBy, String direction) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy != null ? sortBy : DEFAULT_SORT;
        this.direction = direction != null ? direction : DEFAULT_DIRECTION;
    }

    // Static factory methods
    public static Pageable of(int page, int size) {
        return new Pageable(page, size);
    }

    public static Pageable of(int page, int size, String sortBy, String direction) {
        return new Pageable(page, size, sortBy, direction);
    }

    // Helper methods
    public int getOffset() {
        return page * size;
    }

    public String getOrderBy() {
        return sortBy + " " + direction;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size > 0 ? size : DEFAULT_SIZE;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
