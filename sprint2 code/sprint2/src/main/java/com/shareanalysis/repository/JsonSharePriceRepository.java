package com.shareanalysis.repository;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-backed implementation of SharePriceRepository.
 *
 * Sprint 2: Uses an in-memory HashMap (data/share_cache.json integration in Sprint 3).
 * The rest of the application depends only on the SharePriceRepository interface,
 * so swapping this implementation requires no changes elsewhere.
 */
public class JsonSharePriceRepository implements SharePriceRepository {

    private final Map<String, List<SharePrice>> cache = new HashMap<>();

    public JsonSharePriceRepository() {
        new File("data").mkdirs();
        System.out.println("[SharePriceRepository] Initialised.");
    }

    @Override
    public void save(List<SharePrice> prices) {
        if (prices == null || prices.isEmpty()) return;
        String symbol = prices.get(0).getSymbol();

        cache.merge(symbol, new ArrayList<>(prices), (existing, incoming) -> {
            Set<LocalDate> existingDates = existing.stream()
                    .map(SharePrice::getDate).collect(Collectors.toSet());
            incoming.stream()
                    .filter(p -> !existingDates.contains(p.getDate()))
                    .forEach(existing::add);
            existing.sort(Comparator.comparing(SharePrice::getDate));
            return existing;
        });

        System.out.println("[SharePriceRepository] Saved " + prices.size() + " records for: " + symbol);
    }

    @Override
    public List<SharePrice> load(ShareQuery query) {
        return cache.getOrDefault(query.getSymbol(), Collections.emptyList())
                .stream()
                .filter(p -> !p.getDate().isBefore(query.getFrom())
                          && !p.getDate().isAfter(query.getTo()))
                .sorted(Comparator.comparing(SharePrice::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasCachedData(ShareQuery query) {
        return !load(query).isEmpty();
    }

    @Override
    public void evictOlderThan(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        cache.forEach((sym, prices) ->
                prices.removeIf(p -> p.getDate().isBefore(cutoff)));
        System.out.println("[SharePriceRepository] Evicted entries before: " + cutoff);
    }
}
