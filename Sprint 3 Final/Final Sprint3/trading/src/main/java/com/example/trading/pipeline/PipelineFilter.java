package com.example.trading.pipeline;

import com.example.trading.model.SharePrice;
import java.util.List;

/**
 * PIPES AND FILTERS PATTERN — Filter contract
 *
 * Each filter receives a list of SharePrice records, applies a
 * single transformation or validation, and passes the result
 * to the next filter in the pipe.
 *
 * Filters are composable and reusable — they know nothing
 * about each other or the final consumer.
 */
public interface PipelineFilter {
    List<SharePrice> apply(List<SharePrice> input);
    String getFilterName();
}
