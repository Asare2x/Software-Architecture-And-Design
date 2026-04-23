package com.example.trading.component;

import com.example.trading.service.AccountWalletService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * COMPOUND COMPONENT — WatchlistPanel
 *
 * Reusable self-contained watchlist UI component.
 * Accepts an AccountWalletService and an optional callback
 * so a parent screen can react when the user selects a symbol.
 *
 * Architectural pattern: Compound Component with callback-based
 * event propagation (avoids tight coupling to parent).
 */
public class WatchlistPanel extends VBox {

    private final AccountWalletService      walletService;
    private final ObservableList<String>    watchlistData = FXCollections.observableArrayList();
    private Consumer<String>                onSymbolSelected;

    public WatchlistPanel(AccountWalletService walletService) {
        this.walletService = walletService;
        setSpacing(8);
        setPadding(new Insets(12));
        build();
        refresh();
    }

    /** Called when user double-clicks a symbol — e.g. to trigger a search */
    public void setOnSymbolSelected(Consumer<String> callback) {
        this.onSymbolSelected = callback;
    }

    private void build() {
        Label title = new Label("📋  Watchlist");
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;");

        ListView<String> listView = new ListView<>(watchlistData);
        listView.setPrefHeight(200);
        listView.setPlaceholder(new Label("No symbols in watchlist."));
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Double-click to select
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && onSymbolSelected != null) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) onSymbolSelected.accept(selected);
            }
        });

        // Add row
        TextField addField = new TextField();
        addField.setPromptText("Symbol (e.g. AAPL)");
        addField.setPrefHeight(30);
        HBox.setHgrow(addField, Priority.ALWAYS);

        Button addBtn    = new Button("＋ Add");
        Button removeBtn = new Button("✕ Remove");
        addBtn.setPrefHeight(30);
        removeBtn.setPrefHeight(30);
        addBtn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;-fx-background-radius:5;");
        removeBtn.setStyle("-fx-background-color:#757575;-fx-text-fill:white;-fx-background-radius:5;");

        HBox controls = new HBox(6, addField, addBtn, removeBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = new Label("0 symbols");
        countLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#888;");

        getChildren().addAll(title, listView, controls, countLabel);

        // ── Actions ──────────────────────────────────────────────────────
        Runnable doAdd = () -> {
            String sym = addField.getText().trim().toUpperCase();
            if (sym.isEmpty()) return;
            walletService.addToWatchlist(sym);
            refresh();
            countLabel.setText(watchlistData.size() + " symbols");
            addField.clear();
        };

        addBtn.setOnAction(e -> doAdd.run());
        addField.setOnAction(e -> doAdd.run());

        removeBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            walletService.removeFromWatchlist(selected);
            refresh();
            countLabel.setText(watchlistData.size() + " symbols");
        });
    }

    public void refresh() {
        watchlistData.setAll(walletService.getWatchlist());
    }

    public ObservableList<String> getWatchlistData() {
        return watchlistData;
    }
}
