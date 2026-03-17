package com.shareanalysis.service;

import com.shareanalysis.model.Annotation;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Component: Annotation Component
 * Direction: Annotation Component → SharePriceService (provides metadata)
 *
 * Manages user-created annotations (notes) attached to specific
 * dates on a share price chart. Annotations are passed to ChartingService
 * to be overlaid on the rendered chart.
 */
public class AnnotationService {

    private final List<Annotation> annotations = new ArrayList<>();
    private int nextId = 1;

    /** Add a new annotation for a share symbol and date. */
    public Annotation addAnnotation(String symbol, LocalDate date, String note) {
        String id = "ANN-" + nextId++;
        Annotation annotation = new Annotation(id, symbol.toUpperCase(), date, note);
        annotations.add(annotation);
        System.out.println("[AnnotationService] Added: " + annotation);
        return annotation;
    }

    /** Get all annotations for a specific symbol. */
    public List<Annotation> getAnnotations(String symbol) {
        return annotations.stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(symbol))
                .collect(Collectors.toList());
    }

    /** Get all annotations across all symbols. */
    public List<Annotation> getAllAnnotations() {
        return Collections.unmodifiableList(annotations);
    }
}
