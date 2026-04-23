package com.example.trading.component;

/**
 * Controller for StockInfoPanel compound component
 * Separates presentation logic from UI layout
 */
public class StockInfoPanelController {
    
    private final StockInfoPanel view;
    
    public StockInfoPanelController(StockInfoPanel view) {
        this.view = view;
        initializeEventHandlers();
    }
    
    /**
     * Initialize default event handlers
     */
    private void initializeEventHandlers() {
        // Initialize default event handlers (can be overridden)
        if (view.getAddToWatchlistButton() != null) {
            view.getAddToWatchlistButton().setOnAction(e -> {
                System.out.println("Add to watchlist clicked - no action set");
            });
        }
        
        if (view.getBuyStockButton() != null) {
            view.getBuyStockButton().setOnAction(e -> {
                System.out.println("Buy stock clicked - no action set");
            });
        }
    }
    
    /**
     * Update the display with stock information
     */
    public void updateDisplay(String symbol, double price, double change, long volume) {
        if (view.getStockSymbolLabel() != null) {
            view.getStockSymbolLabel().setText(symbol != null ? symbol : "Unknown");
        }
        
        if (view.getStockPriceLabel() != null) {
            view.getStockPriceLabel().setText(String.format("$%.2f", price));
        }
        
        if (view.getStockChangeLabel() != null) {
            // Format change with color
            String changeText = String.format("%.2f (%.2f%%)", change, price != 0 ? (change/price) * 100 : 0);
            view.getStockChangeLabel().setText(changeText);
            
            // Set color based on positive/negative change
            String color = change >= 0 ? "green" : "red";
            view.getStockChangeLabel().setStyle("-fx-text-fill: " + color + ";");
        }
        
        if (view.getStockVolumeLabel() != null) {
            view.getStockVolumeLabel().setText(String.format("Volume: %,d", volume));
        }
    }
    
    /**
     * Set custom action for add to watchlist button
     */
    public void setOnAddToWatchlist(Runnable action) {
        if (view.getAddToWatchlistButton() != null && action != null) {
            view.getAddToWatchlistButton().setOnAction(e -> {
                try {
                    action.run();
                } catch (Exception ex) {
                    System.err.println("Error in add to watchlist action: " + ex.getMessage());
                }
            });
        }
    }
    
    /**
     * Set custom action for buy stock button
     */
    public void setOnBuyStock(Runnable action) {
        if (view.getBuyStockButton() != null && action != null) {
            view.getBuyStockButton().setOnAction(e -> {
                try {
                    action.run();
                } catch (Exception ex) {
                    System.err.println("Error in buy stock action: " + ex.getMessage());
                }
            });
        }
    }
    
    /**
     * Enable/disable the component
     */
    public void setEnabled(boolean enabled) {
        if (view.getAddToWatchlistButton() != null) {
            view.getAddToWatchlistButton().setDisable(!enabled);
        }
        if (view.getBuyStockButton() != null) {
            view.getBuyStockButton().setDisable(!enabled);
        }
    }
    
    /**
     * Clear all displayed information
     */
    public void clearDisplay() {
        updateDisplay("No stock selected", 0.0, 0.0, 0);
        if (view.getStockChangeLabel() != null) {
            view.getStockChangeLabel().setStyle("-fx-text-fill: black;");
        }
    }
}
