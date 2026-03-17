package com.shareanalysis.model;

import java.time.LocalDate;

/**
 * Represents a metadata annotation attached to a specific price point.
 * Component: Annotation Component — sends metadata to SharePriceService.
 */
public class Annotation {

    private final String    annotationId;
    private final String    symbol;
    private final LocalDate date;
    private final String    note;

    public Annotation(String annotationId, String symbol, LocalDate date, String note) {
        this.annotationId = annotationId;
        this.symbol       = symbol;
        this.date         = date;
        this.note         = note;
    }

    public String    getAnnotationId() { return annotationId; }
    public String    getSymbol()       { return symbol;       }
    public LocalDate getDate()         { return date;         }
    public String    getNote()         { return note;         }

    @Override
    public String toString() {
        return String.format("Annotation{symbol='%s', date=%s, note='%s'}", symbol, date, note);
    }
}
