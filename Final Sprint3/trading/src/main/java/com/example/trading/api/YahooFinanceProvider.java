package com.example.trading.api;

import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceProvider implements ShareDataProvider {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private final HttpClient httpClient;

    public YahooFinanceProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException {
        try {
            long period1 = query.getStartDate().atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            long period2 = query.getEndDate().plusDays(1).atStartOfDay(ZoneId.of("UTC")).toEpochSecond();

            String url = BASE_URL + query.getSymbol().toUpperCase()
                    + "?period1=" + period1
                    + "&period2=" + period2
                    + "&interval=1d";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new DataProviderException("Symbol not found: " + query.getSymbol());
            }
            if (response.statusCode() != 200) {
                throw new DataProviderException("HTTP " + response.statusCode() + " from Yahoo Finance");
            }

            List<SharePrice> prices = parseResponse(response.body(), query.getSymbol());
            if (prices.isEmpty()) {
                throw new DataProviderException("No data returned for " + query.getSymbol());
            }
            return prices;

        } catch (DataProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new DataProviderException("Failed to fetch data for " + query.getSymbol() + ": " + e.getMessage(), e);
        }
    }

    private List<SharePrice> parseResponse(String json, String symbol) throws DataProviderException {
        List<SharePrice> prices = new ArrayList<>();
        try {
            String tsKey = "\"timestamp\":[";
            int tsStart = json.indexOf(tsKey);
            if (tsStart < 0) throw new DataProviderException("No timestamp data in response");
            tsStart += tsKey.length();
            int tsEnd = json.indexOf("]", tsStart);
            String[] tsTokens = json.substring(tsStart, tsEnd).split(",");

            String quoteKey = "\"quote\":[{";
            int quoteIdx = json.indexOf(quoteKey);
            if (quoteIdx < 0) throw new DataProviderException("No quote block in response");
            String quoteSection = json.substring(quoteIdx);

            double[] opens   = extractDoubles(quoteSection, "\"open\":[");
            double[] highs   = extractDoubles(quoteSection, "\"high\":[");
            double[] lows    = extractDoubles(quoteSection, "\"low\":[");
            double[] closes  = extractDoubles(quoteSection, "\"close\":[");
            long[]   volumes = extractLongs(quoteSection,   "\"volume\":[");

            int count = Math.min(tsTokens.length, closes.length);
            for (int i = 0; i < count; i++) {
                try {
                    double close = closes[i];
                    if (close == 0.0 || Double.isNaN(close)) continue;

                    long epochSec = Long.parseLong(tsTokens[i].trim());
                    LocalDate date = Instant.ofEpochSecond(epochSec)
                            .atZone(ZoneId.of("America/New_York"))
                            .toLocalDate();

                    double open  = i < opens.length  ? opens[i]  : close;
                    double high  = i < highs.length  ? highs[i]  : close;
                    double low   = i < lows.length   ? lows[i]   : close;
                    long   vol   = i < volumes.length ? volumes[i] : 0L;

                    prices.add(new SharePrice(symbol, date,
                            BigDecimal.valueOf(open),
                            BigDecimal.valueOf(close),
                            BigDecimal.valueOf(high),
                            BigDecimal.valueOf(low),
                            vol));
                } catch (Exception ignored) {}
            }
        } catch (DataProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new DataProviderException("Parse error: " + e.getMessage(), e);
        }
        return prices;
    }

    private double[] extractDoubles(String json, String key) {
        int start = json.indexOf(key);
        if (start < 0) return new double[0];
        start += key.length();
        int end = json.indexOf("]", start);
        if (end < 0) return new double[0];
        String[] parts = json.substring(start, end).split(",");
        double[] vals = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String v = parts[i].trim();
            try { vals[i] = (v.equals("null") || v.isEmpty()) ? 0.0 : Double.parseDouble(v); }
            catch (NumberFormatException e) { vals[i] = 0.0; }
        }
        return vals;
    }

    private long[] extractLongs(String json, String key) {
        int start = json.indexOf(key);
        if (start < 0) return new long[0];
        start += key.length();
        int end = json.indexOf("]", start);
        if (end < 0) return new long[0];
        String[] parts = json.substring(start, end).split(",");
        long[] vals = new long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String v = parts[i].trim();
            try { vals[i] = (v.equals("null") || v.isEmpty()) ? 0L : Long.parseLong(v); }
            catch (NumberFormatException e) { vals[i] = 0L; }
        }
        return vals;
    }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public String getProviderName() { return "Yahoo Finance"; }
}
