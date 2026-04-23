package com.example.trading.pipeline;

import com.example.trading.model.SharePrice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PIPES AND FILTERS PATTERN — Concrete Filters
 *
 * Each inner class is a single-purpose, stateless (or minimally stateful)
 * transformation that can be composed into any pipeline.
 */
public class Filters {

    /** Sorts prices ascending by date */
    public static class SortByDateFilter implements PipelineFilter {
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            return input.stream()
                    .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                    .collect(Collectors.toList());
        }
        @Override public String getFilterName() { return "SortByDate"; }
    }

    /** Removes weekends (no trading data) */
    public static class RemoveWeekendsFilter implements PipelineFilter {
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            return input.stream()
                    .filter(p -> p.getDate().getDayOfWeek() != DayOfWeek.SATURDAY
                              && p.getDate().getDayOfWeek() != DayOfWeek.SUNDAY)
                    .collect(Collectors.toList());
        }
        @Override public String getFilterName() { return "RemoveWeekends"; }
    }

    /** Restricts prices to a date window */
    public static class DateRangeFilter implements PipelineFilter {
        private final LocalDate from;
        private final LocalDate to;
        public DateRangeFilter(LocalDate from, LocalDate to) {
            this.from = from; this.to = to;
        }
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            return input.stream()
                    .filter(p -> !p.getDate().isBefore(from) && !p.getDate().isAfter(to))
                    .collect(Collectors.toList());
        }
        @Override public String getFilterName() { return "DateRange[" + from + "→" + to + "]"; }
    }

    /** Removes records where close price is zero or negative */
    public static class RemoveInvalidPricesFilter implements PipelineFilter {
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            return input.stream()
                    .filter(p -> p.getClosePriceAsDouble() > 0)
                    .collect(Collectors.toList());
        }
        @Override public String getFilterName() { return "RemoveInvalidPrices"; }
    }

    /** Caps to the most recent N records (for chart readability) */
    public static class LimitFilter implements PipelineFilter {
        private final int limit;
        public LimitFilter(int limit) { this.limit = limit; }
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            if (input.size() <= limit) return input;
            return input.subList(input.size() - limit, input.size());
        }
        @Override public String getFilterName() { return "Limit[" + limit + "]"; }
    }

    /** Downsamples to every Nth record to reduce chart point density */
    public static class DownsampleFilter implements PipelineFilter {
        private final int step;
        public DownsampleFilter(int step) { this.step = Math.max(1, step); }
        @Override
        public List<SharePrice> apply(List<SharePrice> input) {
            List<SharePrice> out = new java.util.ArrayList<>();
            for (int i = 0; i < input.size(); i += step) out.add(input.get(i));
            return out;
        }
        @Override public String getFilterName() { return "Downsample[every " + step + "]"; }
    }
}
