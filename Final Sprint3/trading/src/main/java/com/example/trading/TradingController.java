package com.example.trading;

import com.example.trading.api.YahooFinanceProvider;
import com.example.trading.service.IPriceService;
import com.example.trading.service.IAuthService;
import com.example.trading.exception.ServiceException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.service.AuthenticationService;
import com.example.trading.service.SharePriceService;
import com.example.trading.service.PerformanceService;
import com.example.trading.repository.JsonSharePriceRepository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TradingController {

    // ── Toolbar ─────────────────────────────────────────────────────────────
    @FXML private TextField stockSearchField;
    @FXML private Button    searchButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label      statusLabel;

    // ── Stock Info Panel ────────────────────────────────────────────────────
    @FXML private Label  stockSymbolLabel;
    @FXML private Label  stockPriceLabel;
    @FXML private Label  stockChangeLabel;
    @FXML private Label  stockVolumeLabel;
    @FXML private Label  stockDateLabel;
    @FXML private Button addToWatchlistButton;
    @FXML private Button buyStockButton;

    // ── Chart ───────────────────────────────────────────────────────────────
    @FXML private LineChart<String, Number> stockChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis   chartYAxis;

    // ── Details Table ───────────────────────────────────────────────────────
    @FXML private TableView<SharePrice>             stockDetailsTable;
    @FXML private TableColumn<SharePrice, String>   dateColumn;
    @FXML private TableColumn<SharePrice, String>   openColumn;
    @FXML private TableColumn<SharePrice, String>   highColumn;
    @FXML private TableColumn<SharePrice, String>   lowColumn;
    @FXML private TableColumn<SharePrice, String>   closeColumn;
    @FXML private TableColumn<SharePrice, String>   volumeColumn;

    // ── Portfolio Tab ───────────────────────────────────────────────────────
    @FXML private Label              portfolioValueLabel;
    @FXML private Label              totalGainLossLabel;
    @FXML private Label              cashBalanceLabel;
    @FXML private TableView<Object>  portfolioTable;
    @FXML private TableColumn<Object, String> portfolioSymbolColumn;
    @FXML private TableColumn<Object, String> portfolioNameColumn;
    @FXML private TableColumn<Object, String> portfolioSharesColumn;
    @FXML private TableColumn<Object, String> portfolioAvgPriceColumn;
    @FXML private TableColumn<Object, String> portfolioCurrentPriceColumn;
    @FXML private TableColumn<Object, String> portfolioValueColumn;
    @FXML private TableColumn<Object, String> portfolioGainLossColumn;
    @FXML private TableColumn<Object, String> portfolioGainLossPercentColumn;

    // ── Watchlist Tab ───────────────────────────────────────────────────────
    @FXML private TextField          watchlistSearchField;
    @FXML private TableView<Object>  watchlistTable;
    @FXML private TableColumn<Object, String> watchlistSymbolColumn;
    @FXML private TableColumn<Object, String> watchlistPriceColumn;
    @FXML private TableColumn<Object, String> watchlistChangeColumn;
    @FXML private TableColumn<Object, String> watchlistChangePercentColumn;
    @FXML private TableColumn<Object, String> watchlistVolumeColumn;
    @FXML private TableColumn<Object, String> watchlistLastUpdateColumn;

    // ── Status Bar ──────────────────────────────────────────────────────────
    @FXML private Label             connectionStatusLabel;
    @FXML private Label             userLabel;
    @FXML private Label             lastUpdateLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TabPane           mainTabPane;

    // ── Services / State ────────────────────────────────────────────────────
    private IPriceService            sharePriceService;
    private IAuthService             authService;
    private JsonSharePriceRepository repository;
    private ObservableList<SharePrice> currentStockData;
    private String currentSymbol;

    // ════════════════════════════════════════════════════════════════════════
    //  Initialization
    // ════════════════════════════════════════════════════════════════════════

    public void initialize() {
        System.out.println("TradingController.initialize() called");
        try {
            authService  = AuthenticationService.getInstance();
            repository   = new JsonSharePriceRepository();
            repository.loadSampleData();
            YahooFinanceProvider dataProvider = new YahooFinanceProvider();
            sharePriceService = new SharePriceService(repository, dataProvider);

            currentStockData = FXCollections.observableArrayList();

            setupUI();
            setupTableColumns();

            if (userLabel != null)
                userLabel.setText("User: " + (authService.getCurrentUser() != null
                        ? authService.getCurrentUser() : "Unknown"));
            if (statusLabel != null)
                statusLabel.setText("Ready — enter a symbol (e.g. AAPL) or try MSFT, TSLA, GOOGL");
            if (connectionStatusLabel != null)
                connectionStatusLabel.setText("Data: Yahoo Finance (live) + demo fallback");
            if (lastUpdateLabel != null)
                lastUpdateLabel.setText("Last update: never");
            if (endDatePicker != null)
                endDatePicker.setValue(LocalDate.now());
            if (startDatePicker != null)
                startDatePicker.setValue(LocalDate.now().minusDays(30));

            System.out.println("TradingController initialised successfully");
        } catch (Exception e) {
            System.err.println("Init error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Init error: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
    }

    private void setupUI() {
        if (stockSearchField != null)
            stockSearchField.setOnAction(e -> handleStockSearch());
        if (loadingIndicator != null)
            loadingIndicator.setVisible(false);
    }

    private void setupTableColumns() {
        if (stockDetailsTable == null) return;
        try {
            dateColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    cd.getValue().getDate() != null ? cd.getValue().getDate().toString() : ""));
            openColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("$%.2f", cd.getValue().getOpenPriceAsDouble())));
            highColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("$%.2f", cd.getValue().getHighPriceAsDouble())));
            lowColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("$%.2f", cd.getValue().getLowPriceAsDouble())));
            closeColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("$%.2f", cd.getValue().getClosePriceAsDouble())));
            volumeColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("%,d", cd.getValue().getVolume())));
            stockDetailsTable.setItems(currentStockData);
        } catch (Exception e) {
            System.err.println("Table setup error: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Stock Search
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleStockSearch() {
        if (stockSearchField == null) return;
        String symbol = stockSearchField.getText();
        if (symbol == null || symbol.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a stock symbol");
            return;
        }
        symbol = symbol.trim().toUpperCase();
        currentSymbol = symbol;

        stockSymbolLabel.setText(symbol);
        stockPriceLabel.setText("Loading...");
        statusLabel.setText("Fetching " + symbol + "…");
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        searchStockAsync(symbol);
    }

    private void searchStockAsync(String symbol) {
        Task<List<SharePrice>> task = new Task<>() {
            @Override
            protected List<SharePrice> call() throws Exception {
                LocalDate start = startDatePicker.getValue() != null
                        ? startDatePicker.getValue() : LocalDate.now().minusDays(30);
                LocalDate end   = endDatePicker.getValue()   != null
                        ? endDatePicker.getValue()   : LocalDate.now();
                if (start.isAfter(end)) start = end.minusDays(30);
                return sharePriceService.getSharePrices(new ShareQuery(symbol, start, end));
            }

            @Override protected void succeeded() {
                Platform.runLater(() -> {
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    displayStockData(symbol, getValue());
                });
            }

            @Override protected void failed() {
                Platform.runLater(() -> {
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    String msg = "Error loading " + symbol + ": " + getException().getMessage();
                    statusLabel.setText(msg);
                    showAlert(Alert.AlertType.ERROR, "Search Error", msg);
                });
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void displayStockData(String symbol, List<SharePrice> prices) {
        if (prices == null || prices.isEmpty()) {
            statusLabel.setText("No data found for " + symbol);
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No price data found for " + symbol);
            return;
        }

        prices.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        SharePrice latest = prices.get(prices.size() - 1);
        SharePrice first  = prices.get(0);

        // Labels
        stockPriceLabel.setText(String.format("$%.2f", latest.getClosePriceAsDouble()));
        stockVolumeLabel.setText("Volume: " + String.format("%,d", latest.getVolume()));
        stockDateLabel.setText("Date: " + latest.getDate());
        lastUpdateLabel.setText("Last update: " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        statusLabel.setText(String.format("Loaded %d records for %s", prices.size(), symbol));

        // Change label
        double change = latest.getClosePriceAsDouble() - first.getClosePriceAsDouble();
        double changePct = first.getClosePriceAsDouble() != 0
                ? (change / first.getClosePriceAsDouble()) * 100 : 0;
        stockChangeLabel.setText(String.format("%s%.2f (%.2f%%)",
                change >= 0 ? "+" : "", change, changePct));
        stockChangeLabel.setStyle(change >= 0
                ? "-fx-text-fill: #28a745;" : "-fx-text-fill: #dc3545;");

        // Table
        currentStockData.clear();
        currentStockData.addAll(prices);

        // Chart
        if (stockChart != null) {
            stockChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(symbol);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
            // Limit to last 60 points to keep the chart readable
            int start = Math.max(0, prices.size() - 60);
            for (int i = start; i < prices.size(); i++) {
                SharePrice p = prices.get(i);
                series.getData().add(new XYChart.Data<>(
                        p.getDate().format(fmt),
                        p.getClosePriceAsDouble()));
            }
            stockChart.getData().add(series);
            if (chartXAxis != null) chartXAxis.setLabel("Date");
            if (chartYAxis != null) chartYAxis.setLabel("Price (USD)");
        }
    }

    @FXML
    private void handleDateRangeUpdate() {
        if (currentSymbol != null) handleStockSearch();
        else showAlert(Alert.AlertType.WARNING, "No Stock", "Please search for a stock first");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Stock Comparison  — proper chart window
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handlePriceComparison() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Stock Comparison");
        dialog.setWidth(900);
        dialog.setHeight(620);

        // ── Input row ────────────────────────────────────────────────────────
        TextField sym1Field = new TextField(currentSymbol != null ? currentSymbol : "AAPL");
        TextField sym2Field = new TextField("MSFT");
        DatePicker dp1 = new DatePicker(LocalDate.now().minusDays(90));
        DatePicker dp2 = new DatePicker(LocalDate.now());
        Button compareBtn = new Button("Compare");
        compareBtn.setDefaultButton(true);

        sym1Field.setPromptText("Symbol 1");
        sym2Field.setPromptText("Symbol 2");
        sym1Field.setPrefWidth(100);
        sym2Field.setPrefWidth(100);

        HBox inputRow = new HBox(10,
                new Label("Symbol 1:"), sym1Field,
                new Label("Symbol 2:"), sym2Field,
                new Label("From:"), dp1,
                new Label("To:"), dp2,
                compareBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(10));

        // ── Chart (normalised to 100 at start) ───────────────────────────────
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Indexed Price (base = 100)");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Normalised Performance Comparison");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);

        // ── Summary table ────────────────────────────────────────────────────
        TableView<String[]> summaryTable = new TableView<>();
        summaryTable.setMaxHeight(140);
        summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for (String col : new String[]{"Symbol", "Start Price", "End Price", "Change", "% Change", "High", "Low"}) {
            TableColumn<String[], String> tc = new TableColumn<>(col);
            int idx = summaryTable.getColumns().size();
            tc.setCellValueFactory(cd ->
                    new javafx.beans.property.SimpleStringProperty(cd.getValue()[idx]));
            summaryTable.getColumns().add(tc);
        }

        Label statusLbl = new Label("Enter two symbols and click Compare");
        statusLbl.setPadding(new Insets(4, 10, 4, 10));

        // ── Layout ────────────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(inputRow);
        root.setCenter(chart);
        VBox bottom = new VBox(5, summaryTable, statusLbl);
        bottom.setPadding(new Insets(5, 10, 10, 10));
        root.setBottom(bottom);

        // ── Compare action ────────────────────────────────────────────────────
        compareBtn.setOnAction(e -> {
            String s1 = sym1Field.getText().trim().toUpperCase();
            String s2 = sym2Field.getText().trim().toUpperCase();
            if (s1.isEmpty() || s2.isEmpty()) {
                statusLbl.setText("Please enter both symbols");
                return;
            }
            LocalDate from = dp1.getValue() != null ? dp1.getValue() : LocalDate.now().minusDays(90);
            LocalDate to   = dp2.getValue() != null ? dp2.getValue() : LocalDate.now();

            compareBtn.setDisable(true);
            statusLbl.setText("Loading " + s1 + " and " + s2 + "…");

            Task<Void> task = new Task<>() {
                List<SharePrice> p1, p2;

                @Override
                protected Void call() throws Exception {
                    p1 = sharePriceService.getSharePrices(new ShareQuery(s1, from, to));
                    p2 = sharePriceService.getSharePrices(new ShareQuery(s2, from, to));
                    return null;
                }

                @Override protected void succeeded() {
                    Platform.runLater(() -> {
                        compareBtn.setDisable(false);
                        renderComparison(chart, summaryTable, statusLbl, s1, s2, p1, p2);
                    });
                }

                @Override protected void failed() {
                    Platform.runLater(() -> {
                        compareBtn.setDisable(false);
                        statusLbl.setText("Error: " + getException().getMessage());
                    });
                }
            };
            new Thread(task, "compare-thread").start();
        });

        dialog.setScene(new Scene(root));
        dialog.show();
    }

    private void renderComparison(
            LineChart<String, Number> chart,
            TableView<String[]> summaryTable,
            Label statusLbl,
            String s1, String s2,
            List<SharePrice> p1, List<SharePrice> p2) {

        chart.getData().clear();
        summaryTable.getItems().clear();

        if (p1 == null || p1.isEmpty() || p2 == null || p2.isEmpty()) {
            statusLbl.setText("No data available for one or both symbols");
            return;
        }

        p1.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        p2.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");

        XYChart.Series<String, Number> series1 = buildNormalisedSeries(s1, p1, fmt);
        XYChart.Series<String, Number> series2 = buildNormalisedSeries(s2, p2, fmt);
        chart.getData().addAll(series1, series2);

        // Summary rows
        summaryTable.getItems().add(buildSummaryRow(s1, p1));
        summaryTable.getItems().add(buildSummaryRow(s2, p2));

        statusLbl.setText(String.format("Comparing %s vs %s — %d and %d data points",
                s1, s2, p1.size(), p2.size()));
    }

    private XYChart.Series<String, Number> buildNormalisedSeries(
            String name, List<SharePrice> prices, DateTimeFormatter fmt) {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        double base = prices.get(0).getClosePriceAsDouble();
        if (base == 0) base = 1;

        int step = Math.max(1, prices.size() / 60);  // show at most ~60 ticks
        for (int i = 0; i < prices.size(); i += step) {
            SharePrice p = prices.get(i);
            double indexed = (p.getClosePriceAsDouble() / base) * 100.0;
            series.getData().add(new XYChart.Data<>(p.getDate().format(fmt), indexed));
        }
        return series;
    }

    private String[] buildSummaryRow(String symbol, List<SharePrice> prices) {
        double start = prices.get(0).getClosePriceAsDouble();
        double end   = prices.get(prices.size() - 1).getClosePriceAsDouble();
        double change = end - start;
        double pct   = start != 0 ? (change / start) * 100 : 0;
        double high  = prices.stream().mapToDouble(SharePrice::getHighPriceAsDouble).max().orElse(0);
        double low   = prices.stream().mapToDouble(SharePrice::getLowPriceAsDouble).min().orElse(0);
        return new String[]{
            symbol,
            String.format("$%.2f", start),
            String.format("$%.2f", end),
            String.format("%s$%.2f", change >= 0 ? "+" : "", change),
            String.format("%.2f%%", pct),
            String.format("$%.2f", high),
            String.format("$%.2f", low)
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Performance Report
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handlePerformanceReport() {
        if (currentSymbol == null) {
            showAlert(Alert.AlertType.WARNING, "No Stock", "Please search for a stock first");
            return;
        }
        try {
            PerformanceService ps = new PerformanceService(sharePriceService);
            PerformanceService.PerformanceSummary s = ps.getPerformanceSummary(
                    currentSymbol, LocalDate.now().minusDays(30), LocalDate.now());
            showAlert(Alert.AlertType.INFORMATION, "Performance Report",
                String.format("Performance Report for %s%n%nPrice Change: $%.2f (%.2f%%)%nHigh: $%.2f%nLow: $%.2f",
                    s.getSymbol(),
                    s.getTotalReturn()      != null ? s.getTotalReturn().doubleValue()      : 0,
                    s.getPercentageReturn() != null ? s.getPercentageReturn().doubleValue() : 0,
                    s.getHighPrice()        != null ? s.getHighPrice().doubleValue()        : 0,
                    s.getLowPrice()         != null ? s.getLowPrice().doubleValue()         : 0));
        } catch (ServiceException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Performance report failed: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Logout / Exit
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setContentText("Are you sure you want to logout?");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                authService.logout();
                returnToLogin();
            }
        });
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setContentText("Are you sure you want to exit?");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { Platform.exit(); System.exit(0); }
        });
    }

    private void returnToLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 500, 400));
            stage.setTitle("Trading System - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Stub handlers (not yet implemented)
    // ════════════════════════════════════════════════════════════════════════

    @FXML private void handleNewPortfolio()        { notYet("Portfolio management"); }
    @FXML private void handleOpenPortfolio()       { notYet("Open portfolio"); }
    @FXML private void handleExportData()          { notYet("Data export"); }
    @FXML private void handleBuyStock()            { notYet("Buy stock"); }
    @FXML private void handleSellStock()           { notYet("Sell stock"); }
    @FXML private void handleViewTransactions()    { notYet("Transaction history"); }
    @FXML private void handleAlerts()              { notYet("Alert management"); }
    @FXML private void handleAddToWatchlist()      { notYet("Watchlist"); }
    @FXML private void handleRefreshWatchlist()    { notYet("Watchlist refresh"); }
    @FXML private void handleRemoveFromWatchlist() { notYet("Remove from watchlist"); }

    @FXML
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About",
                "Trading System v1.1\nBuilt with JavaFX\n\nLogin → search stocks → compare performance");
    }

    @FXML
    private void handleUserGuide() {
        showAlert(Alert.AlertType.INFORMATION, "User Guide",
                "1. Enter a stock symbol (e.g. AAPL) in the search bar\n"
              + "2. Choose a date range and press Search\n"
              + "3. View the chart and OHLCV table\n"
              + "4. Use Analysis → Price Comparison to compare two stocks side-by-side");
    }

    private void notYet(String feature) {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", feature + " is not yet implemented.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
