package com.example.trading.component;

import com.example.trading.model.SharePrice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * COMPOUND COMPONENT — PriceTickerPanel
 *
 * Displays symbol, current price, and change information
 * in a compact, reusable ticker strip.
 *
 * Can be embedded in any screen or toolbar.
 * Updated by calling update(SharePrice, SharePrice) — no
 * service dependency, pure display component.
 *
 * Architectural pattern: Passive View (receives data, does not fetch).
 */
public class PriceTickerPanel extends HBox {

    private final Label symbolLabel;
    private final Label priceLabel;
    private final Label changeLabel;
    private final Label volumeLabel;
    private final Label dateLabel;

    public PriceTickerPanel() {
        setSpacing(24);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(12, 16, 12, 16));
        setStyle("-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-width:0 0 1 0;");

        symbolLabel = new Label("—");
        symbolLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;");

        priceLabel = new Label("—");
        priceLabel.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:#1a73e8;");

        changeLabel = new Label("");
        changeLabel.setStyle("-fx-font-size:14px;");

        volumeLabel = new Label("");
        volumeLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#555;");

        dateLabel = new Label("");
        dateLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#888;");

        VBox leftBox  = new VBox(2, symbolLabel, priceLabel);
        VBox rightBox = new VBox(4, changeLabel, volumeLabel, dateLabel);

        getChildren().addAll(
            leftBox,
            new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL),
            rightBox
        );
    }

    /**
     * Update the ticker display.
     *
     * @param latest  the most recent price record
     * @param first   the first record in the period (used to calculate change)
     */
    public void update(SharePrice latest, SharePrice first) {
        if (latest == null) { reset(); return; }

        symbolLabel.setText(latest.getSymbol());
        priceLabel.setText(String.format("$%.2f", latest.getClosePriceAsDouble()));
        dateLabel.setText("As of: " + latest.getDate());
        volumeLabel.setText(String.format("Volume: %,d", latest.getVolume()));

        if (first != null && first.getClosePriceAsDouble() != 0) {
            double change    = latest.getClosePriceAsDouble() - first.getClosePriceAsDouble();
            double changePct = (change / first.getClosePriceAsDouble()) * 100;
            boolean up = change >= 0;
            changeLabel.setText(String.format("%s$%.2f  (%.2f%%)", up ? "▲ +" : "▼ ", change, changePct));
            changeLabel.setStyle("-fx-font-size:14px;-fx-text-fill:" + (up ? "#2e7d32" : "#c62828") + ";");
        } else {
            changeLabel.setText("");
        }
    }

    /** Reset the panel to its empty state */
    public void reset() {
        symbolLabel.setText("—");
        priceLabel.setText("—");
        changeLabel.setText("");
        volumeLabel.setText("");
        dateLabel.setText("");
    }

    // Individual setters for programmatic control
    public void setSymbol(String symbol)  { symbolLabel.setText(symbol); }
    public void setPrice(double price)    { priceLabel.setText(String.format("$%.2f", price)); }
}
