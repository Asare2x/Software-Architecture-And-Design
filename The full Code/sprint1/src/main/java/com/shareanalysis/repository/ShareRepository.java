package com.shareanalysis.repository;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;
import java.util.Optional;

/**
 * Component Interface: ShareRepository
 *
 * Defines the contract for persistent local storage of share-price data.
 * Decouples the service layer from the storage mechanism (JSON, SQLite, etc.).
 *
 * Offline support requirement: if the application cannot reach an external
 * data provider, it will fall back to data held in this repository.
 */
public interface ShareRepository {

    /**
     * Persist a list of share prices to local storage.
     *
     * @param prices the prices to save; must not be null or empty
     */
    void save(List<SharePrice> prices);

    /**
     * Retrieve locally stored prices that match the given query.
     *
     * @param query the query describing symbol and date range
     * @return matching prices (may be an empty list if none cached)
     */
    List<SharePrice> load(ShareQuery query);

    /**
     * Check whether any data is cached for the given query range.
     *
     * @param query the query to check
     * @return true if the repository can fully satisfy the query offline
     */
    boolean hasCachedData(ShareQuery query);

    /**
     * Remove all cached data older than the given number of days.
     *
     * @param days entries older than this threshold are deleted
     */
    void evictOlderThan(int days);
}
