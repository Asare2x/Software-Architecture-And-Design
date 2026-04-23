package com.example.trading.pipeline;

import com.example.trading.model.SharePrice;

import java.util.ArrayList;
import java.util.List;

/**
 * PIPES AND FILTERS PATTERN — Pipeline (the Pipe)
 *
 * Chains multiple PipelineFilter instances together.
 * Data flows left to right through each filter in sequence.
 *
 * Usage:
 *   List<SharePrice> result = new FilterPipeline()
 *       .addFilter(new SortByDateFilter())
 *       .addFilter(new DateRangeFilter(from, to))
 *       .addFilter(new RemoveWeekendFilter())
 *       .addFilter(new NormaliseFilter())
 *       .execute(rawPrices);
 */
public class FilterPipeline {

    private final List<PipelineFilter> filters = new ArrayList<>();

    public FilterPipeline addFilter(PipelineFilter filter) {
        filters.add(filter);
        return this;   // fluent API — enables chaining
    }

    public List<SharePrice> execute(List<SharePrice> input) {
        List<SharePrice> data = new ArrayList<>(input);
        for (PipelineFilter filter : filters) {
            data = filter.apply(data);
        }
        return data;
    }

    public List<String> getFilterNames() {
        return filters.stream().map(PipelineFilter::getFilterName).toList();
    }

    public int size() { return filters.size(); }
}
