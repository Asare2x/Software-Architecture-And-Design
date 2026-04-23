package com.example.trading.component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Compound component for displaying stock information
 * Implements reusable UI component pattern
 */
public class StockInfoPanel extends HBox {
    
    @FXML private Label stockSymbolLabel;
    @FXML private Label stockPriceLabel;
    @FXML private Label stockChangeLabel;
    @FXML private Label stockVolumeLabel;
    @FXML private Button addToWatchlistButton;
    @FXML private Button buyStockButton;
    
    private StockInfoPanelController controller;
    
    public StockInfoPanel() {
        // Try to load FXML, but fallback to programmatic creation if file doesn't exist
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/trading/component/stock-info-panel.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            // If FXML loading fails, create components programmatically
            createComponentsProgrammatically();
        }
        
        this.controller = new StockInfoPanelController(this);
    }
    
    /**
     * Create UI components programmatically if FXML is not available
     */
    private void createComponentsProgrammatically() {
        // Create labels
        stockSymbolLabel = new Label("No stock selected");
        stockSymbolLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        
        stockPriceLabel = new Label("");
        stockPriceLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #007bff;");
        
        stockChangeLabel = new Label("");
        stockVolumeLabel = new Label("");
        
        // Create buttons
        addToWatchlistButton = new Button("Add to Watchlist");
        buyStockButton = new Button("Buy Stock");
        buyStockButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        
        // Create layout
        VBox leftInfo = new VBox(5, stockSymbolLabel, stockPriceLabel);
        VBox middleInfo = new VBox(3, stockChangeLabel, stockVolumeLabel);
        VBox rightButtons = new VBox(10, addToWatchlistButton, buyStockButton);
        
        // Add to main container
        this.getChildren().addAll(leftInfo, middleInfo, rightButtons);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-padding: 15;");
    }
    
    /**
     * Update stock information display
     */
    public void updateStockInfo(String symbol, double price, double change, long volume) {
        controller.updateDisplay(symbol, price, change, volume);
    }
    
    /**
     * Set action for add to watchlist button
     */
    public void setOnAddToWatchlist(Runnable action) {
        controller.setOnAddToWatchlist(action);
    }
    
    /**
     * Set action for buy stock button
     */
    public void setOnBuyStock(Runnable action) {
        controller.setOnBuyStock(action);
    }
    
    // Getters for controller access
    public Label getStockSymbolLabel() { return stockSymbolLabel; }
    public Label getStockPriceLabel() { return stockPriceLabel; }
    public Label getStockChangeLabel() { return stockChangeLabel; }
    public Label getStockVolumeLabel() { return stockVolumeLabel; }
    public Button getAddToWatchlistButton() { return addToWatchlistButton; }
    public Button getBuyStockButton() { return buyStockButton; }
}
