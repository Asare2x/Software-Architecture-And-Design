package com.shareanalysis.repository;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-file-based implementation of {@link ShareRepository}.
 *
 * Data is stored in a flat JSON file under the working directory:
 *   data/share_cache.json
 *
 * Sprint 1 Note: The serialisation logic is a hand-rolled stub so that
 * the project compiles without external libraries. In Sprint 2 the
 * implementation will switch to the Gson or Jackson library for robust
 * JSON handling.
 *
 * Architecture role: Data Layer / Persistence Component
 */
public class JsonShareRepository implements ShareRepository {

    /** Path to the local JSON cache file. */
    private static final String CACHE_FILE = "data/share_cache.json";

    /** In-memory store used during a single session (Sprint 1 stub). */
    private final Map<String, List<SharePrice>> cache = new HashMap<>();

    public JsonShareRepository() {
        // Ensure the data directory exists
        new File("data").mkdirs();
        System.out.println("[JsonShareRepository] Initialised. Cache directory: data/");
    }

    /**
     * Saves prices into the in-memory cache.
     *
     * Sprint 1: In-memory only.
     * Sprint 2: Will serialise to share_cache.json using Gson.
     */
    @Override
    public void save(List<SharePrice> prices) {
        if (prices == null || prices.isEmpty()) return;

        String symbol = prices.get(0).getSymbol();
        cache.merge(symbol, prices, (existing, incoming) -> {
            Set<LocalDate> existingDates = existing.stream()
                    .map(SharePrice::getDate)
                    .collect(Collectors.toSet());
            // Only add records not already cached
            incoming.stream()
                    .filter(p -> !existingDates.contains(p.getDate()))
                    .forEach(existing::add);
            existing.sort(Comparator.comparing(SharePrice::getDate));
            return existing;
        });

        System.out.println("[JsonShareRepository] Saved " + prices.size()
                + " records for symbol: " + symbol);
    }

    /**
     * Retrieves prices from the in-memory cache that match the query.
     */
    @Override
    public List<SharePrice> load(ShareQuery query) {
        List<SharePrice> allForSymbol = cache.getOrDefault(query.getSymbol(), Collections.emptyList());

        return allForSymbol.stream()
                .filter(p -> !p.getDate().isBefore(query.getFrom())
                          && !p.getDate().isAfter(query.getTo()))
                .sorted(Comparator.comparing(SharePrice::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the cache contains at least one record in the requested range.
     *
     * Sprint 1: Simple presence check. Sprint 2 will validate completeness.
     */
    @Override
    public boolean hasCachedData(ShareQuery query) {
        return !load(query).isEmpty();
    }

    /**
     * Removes in-memory entries older than the specified number of days.
     */
    @Override
    public void evictOlderThan(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        cache.forEach((symbol, prices) ->
                prices.removeIf(p -> p.getDate().isBefore(cutoff)));
        System.out.println("[JsonShareRepository] Evicted entries older than " + cutoff);
    }
}
