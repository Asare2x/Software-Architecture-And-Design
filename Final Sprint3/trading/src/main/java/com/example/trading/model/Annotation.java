package com.example.trading.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Annotation {
    private String annotationId;
    private String symbol;
    private LocalDate date;
    private String title;
    private String description;
    private AnnotationType type;
    private String userId;
    private LocalDateTime createdAt;
    
    public enum AnnotationType {
        BUY_SIGNAL,
        SELL_SIGNAL,
        NEWS_EVENT,
        TECHNICAL_ANALYSIS,
        PERSONAL_NOTE
    }
    
    public Annotation() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Annotation(String symbol, LocalDate date, String title, String description, AnnotationType type) {
        this();
        this.symbol = symbol;
        this.date = date;
        this.title = title;
        this.description = description;
        this.type = type;
    }
    
    // Getters and setters
    public String getAnnotationId() {
        return annotationId;
    }
    
    public void setAnnotationId(String annotationId) {
        this.annotationId = annotationId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AnnotationType getType() {
        return type;
    }
    
    public void setType(AnnotationType type) {
        this.type = type;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Annotation{" +
                "annotationId='" + annotationId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", userId='" + userId + '\'' +
                '}';
    }
}
