package com.example.demo1.repository;

import com.example.demo1.model.SharePrice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-based implementation of SharePriceRepository.
 * Persists share price data locally as JSON files for offline access.
 * Full read/write implementation to be completed in Sprint 2.
 */
public class JsonSharePriceRepository implements SharePriceRepository {

    // TODO Sprint 2: Define a storage path and implement JSON serialisation
    // e.g. private static final String STORAGE_DIR = "data/prices/";

    @Override
    public void savePrices(List<SharePrice> prices) {
        // TODO Sprint 2: Serialise prices to JSON and write to disk
        System.out.println("[JsonRepository] savePrices() called — stub, not yet implemented.");
    }

    @Override
    public List<SharePrice> getPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        // TODO Sprint 2: Read and deserialise JSON from disk
        System.out.println("[JsonRepository] getPrices() called — stub, returning empty list.");
        return new ArrayList<>();
    }

    @Override
    public boolean hasData(String symbol, LocalDate startDate, LocalDate endDate) {
        // TODO Sprint 2: Check if JSON file exists for given symbol/range
        return false;
    }
}
