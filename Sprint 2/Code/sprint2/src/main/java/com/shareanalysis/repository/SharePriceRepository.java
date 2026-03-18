package com.shareanalysis.repository;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;

/**
 * Component: SharePriceRepository
 * Direction: SharePriceRepository → SharePriceService (provides stored data)
 *
 * Interface for local persistence of share price data.
 * Enables offline access when the external API is unavailable.
 */
public interface SharePriceRepository {

    /** Save a list of share prices to local storage. */
    void save(List<SharePrice> prices);

    /** Load stored prices matching the query. */
    List<SharePrice> load(ShareQuery query);

    /** True if cached data exists for the given query range. */
    boolean hasCachedData(ShareQuery query);

    /** Remove entries older than the given number of days. */
    void evictOlderThan(int days);
}
