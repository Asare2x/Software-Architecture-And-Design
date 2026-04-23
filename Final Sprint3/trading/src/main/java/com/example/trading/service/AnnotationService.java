package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.Annotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnnotationService {
    private final IAuthService authService;
    private final List<Annotation> annotations; // In-memory storage for now
    
    public AnnotationService(IAuthService authService) {
        this.authService = authService;
        this.annotations = new ArrayList<>();
    }
    
    /**
     * Create a new annotation
     */
    public Annotation createAnnotation(String symbol, LocalDate date, String title, 
                                     String description, Annotation.AnnotationType type) {
        try {
            String currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new ServiceException("No user logged in");
            }
            
            Annotation annotation = new Annotation(symbol.toUpperCase(), date, title, description, type);
            annotation.setAnnotationId(UUID.randomUUID().toString());
            annotation.setUserId(currentUser);
            
            annotations.add(annotation);
            return annotation;
            
        } catch (Exception e) {
            throw new ServiceException("Error creating annotation", e);
        }
    }
    
    /**
     * Get annotations for a symbol
     */
    public List<Annotation> getAnnotationsForSymbol(String symbol) {
        return annotations.stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(symbol))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get annotations for current user
     */
    public List<Annotation> getCurrentUserAnnotations() {
        String currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return annotations.stream()
                .filter(a -> currentUser.equals(a.getUserId()))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get annotations for a date range
     */
    public List<Annotation> getAnnotationsForDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        return annotations.stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(symbol))
                .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                .sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Update an annotation
     */
    public void updateAnnotation(String annotationId, String title, String description) {
        try {
            String currentUser = authService.getCurrentUser();
            Annotation annotation = annotations.stream()
                    .filter(a -> a.getAnnotationId().equals(annotationId))
                    .filter(a -> currentUser.equals(a.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("Annotation not found"));
            
            annotation.setTitle(title);
            annotation.setDescription(description);
            
        } catch (Exception e) {
            throw new ServiceException("Error updating annotation", e);
        }
    }
    
    /**
     * Delete an annotation
     */
    public void deleteAnnotation(String annotationId) {
        try {
            String currentUser = authService.getCurrentUser();
            boolean removed = annotations.removeIf(a -> 
                a.getAnnotationId().equals(annotationId) && currentUser.equals(a.getUserId()));
            
            if (!removed) {
                throw new ServiceException("Annotation not found");
            }
            
        } catch (Exception e) {
            throw new ServiceException("Error deleting annotation", e);
        }
    }
    
    /**
     * Get annotations by type
     */
    public List<Annotation> getAnnotationsByType(Annotation.AnnotationType type) {
        String currentUser = authService.getCurrentUser();
        return annotations.stream()
                .filter(a -> currentUser.equals(a.getUserId()))
                .filter(a -> a.getType() == type)
                .collect(Collectors.toList());
    }
}
