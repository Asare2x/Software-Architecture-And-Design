package com.example.trading.component;

import com.example.trading.model.Alert;
import com.example.trading.service.IAlert;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * COMPOUND COMPONENT — AlertPanel
 *
 * A self-contained, reusable UI component that encapsulates
 * all alert management UI and logic.
 *
 * Can be dropped into any container in the application.
 * Depends only on IAlert (interface), not any concrete service.
 *
 * Architectural pattern: Compound Component with MVC separation.
 *   - This class is the View
 *   - IAlert is the Model/Service
 *   - User interactions delegate to IAlert (Controller role)
 */
public class AlertPanel extends VBox {

    private final IAlert alertService;
    private final ObservableList<Alert> alertData = FXCollections.observableArrayList();
    private Runnable onStatusUpdate;

    public AlertPanel(IAlert alertService) {
        this.alertService = alertService;
        setSpacing(0);
        build();
        refresh();
    }

    /** Optional callback to push status messages to a parent component */
    public void setOnStatusUpdate(Runnable callback) {
        this.onStatusUpdate = callback;
    }

    private void build() {
        // ── Create row ─────────────────────────────────────────────────────
        TextField symField   = new TextField(); symField.setPromptText("Symbol"); symField.setPrefWidth(110); symField.setPrefHeight(30);
        TextField priceField = new TextField(); priceField.setPromptText("Target $"); priceField.setPrefWidth(110); priceField.setPrefHeight(30);
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("PRICE_ABOVE","PRICE_BELOW","PERCENT_CHANGE_UP","PERCENT_CHANGE_DOWN");
        typeBox.setValue("PRICE_ABOVE"); typeBox.setPrefHeight(30);

        Button addBtn   = styledBtn("＋ Add Alert",   "#1a73e8");
        Button checkBtn = styledBtn("⚡ Check Now",    "#f57c00");
        Label  formStatus = new Label("");

        HBox createRow = new HBox(8, new Label("Symbol:"), symField,
                new Label("Target $:"), priceField, new Label("Type:"), typeBox,
                addBtn, checkBtn, formStatus);
        createRow.setAlignment(Pos.CENTER_LEFT);
        createRow.setPadding(new Insets(10, 12, 10, 12));
        createRow.setStyle("-fx-background-color:#fafafa;-fx-border-color:#e0e0e0;-fx-border-width:0 0 1 0;");

        // ── Table ──────────────────────────────────────────────────────────
        TableView<Alert> table = new TableView<>(alertData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No alerts set."));
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().addAll(
            strCol("ID",      a -> a.getAlertId()),
            strCol("Symbol",  a -> a.getSymbol()),
            strCol("Type",    a -> a.getAlertType().name()),
            strCol("Target",  a -> String.format("$%.2f", a.getTargetPrice().doubleValue())),
            strCol("Status",  a -> a.isActive() ? "🟢 Active" : "🔴 Triggered")
        );

        // Cancel action column
        TableColumn<Alert, String> cancelCol = new TableColumn<>("Action");
        cancelCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cancel");
            { btn.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-background-radius:4;-fx-font-size:11px;");
              btn.setOnAction(e -> {
                Alert a = getTableView().getItems().get(getIndex());
                alertService.cancelAlert(a.getAlertId());
                refresh();
              });
            }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : btn);
            }
        });
        table.getColumns().add(cancelCol);

        // ── Stats bar ──────────────────────────────────────────────────────
        Label statsLabel = new Label("No alerts yet.");
        statsLabel.setPadding(new Insets(5, 12, 5, 12));
        statsLabel.setStyle("-fx-text-fill:#555;-fx-font-size:12px;");

        getChildren().addAll(createRow, table, statsLabel);

        // ── Actions ────────────────────────────────────────────────────────
        addBtn.setOnAction(e -> {
            try {
                String sym = symField.getText().trim().toUpperCase();
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                Alert.AlertType type = Alert.AlertType.valueOf(typeBox.getValue());
                alertService.createAlert(sym, price, type);
                refresh();
                refreshStats(statsLabel);
                symField.clear(); priceField.clear();
                formStatus.setText("✅ Created"); formStatus.setStyle("-fx-text-fill:#2e7d32;");
            } catch (NumberFormatException ex) {
                formStatus.setText("❌ Invalid price"); formStatus.setStyle("-fx-text-fill:#e53935;");
            } catch (Exception ex) {
                formStatus.setText("❌ " + ex.getMessage()); formStatus.setStyle("-fx-text-fill:#e53935;");
            }
        });

        checkBtn.setOnAction(e -> {
            List<Alert> triggered = alertService.checkAlerts();
            refresh(); refreshStats(statsLabel);
            formStatus.setText(triggered.isEmpty() ? "No alerts triggered" :
                    "⚡ " + triggered.size() + " triggered!");
            formStatus.setStyle(triggered.isEmpty() ? "-fx-text-fill:#555;" : "-fx-text-fill:#f57c00;-fx-font-weight:bold;");
        });
    }

    public void refresh() {
        alertData.setAll(alertService.getAllAlerts());
    }

    private void refreshStats(Label statsLabel) {
        com.example.trading.service.AlertService svc = (com.example.trading.service.AlertService) alertService;
        var stats = svc.getAlertStatistics();
        statsLabel.setText(String.format("Total: %d  |  Active: %d  |  Triggered: %d  |  Cancelled: %d",
                stats.getTotalAlerts(), stats.getActiveAlerts(), stats.getTriggeredAlerts(), stats.getCancelledAlerts()));
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Alert, String> strCol(String title, java.util.function.Function<Alert, String> fn) {
        TableColumn<Alert, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return c;
    }

    private Button styledBtn(String text, String color) {
        Button b = new Button(text); b.setPrefHeight(30);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");
        return b;
    }
}
