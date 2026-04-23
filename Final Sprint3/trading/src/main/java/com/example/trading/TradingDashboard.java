package com.example.trading;

import com.example.trading.component.AlertPanel;
import com.example.trading.component.WatchlistPanel;
import com.example.trading.component.PriceTickerPanel;
import com.example.trading.soa.ServiceRegistry;
import com.example.trading.service.IPriceService;
import com.example.trading.model.Alert;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.repository.InMemoryAccountRepository;
import com.example.trading.repository.JsonSharePriceRepository;
import com.example.trading.service.*;
import com.example.trading.api.YahooFinanceProvider;
import java.math.BigDecimal;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pure-Java trading dashboard — no FXML dependency.
 * Wires all services internally.
 */
public class TradingDashboard {

    private final Stage              stage;
    private final IAuthService       authService;
    private final IPriceService      priceService;
    private final IPerformance       perfService;
    private final PriceComparisonService compService;
    private final AlertService alertService;
    private final AccountWalletService walletService;

    private Label statusLabel;

    public TradingDashboard(Stage stage, IAuthService authService) {
        this.authService = authService;

        JsonSharePriceRepository repo = new JsonSharePriceRepository();
        repo.loadSampleData();
        YahooFinanceProvider provider = new YahooFinanceProvider();
        SharePriceService sps = new SharePriceService(repo, provider);

        this.priceService  = sps;
        this.perfService   = new PerformanceService(sps);
        this.compService   = new PriceComparisonService(sps);
        InMemoryAccountRepository accountRepo = new InMemoryAccountRepository();
        this.walletService = new AccountWalletService(accountRepo, authService);
        this.alertService  = new AlertService(walletService, sps, accountRepo);
        // Register all services in SOA ServiceRegistry for discovery & interoperability
        ServiceRegistry registry = ServiceRegistry.getInstance();
        registry.registerService(IPriceService.class, sps);
        registry.registerService(IAuthService.class, authService);
        registry.registerService(IAlert.class, this.alertService);
        registry.registerService(IPerformance.class, this.perfService);
        registry.registerService("AccountWalletService", this.walletService);
        registry.registerService("PriceComparisonService", this.compService);

        this.stage         = stage;
    }

    public void show() {
        stage.setTitle("Trading System — " + authService.getCurrentUser());
        stage.setScene(buildScene());
        stage.setWidth(1300);
        stage.setHeight(820);
        stage.centerOnScreen();
        stage.show();
    }

    // ── Scene ────────────────────────────────────────────────────────────────

    private Scene buildScene() {
        // Top bar
        Label title = new Label("📈 Trading System");
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label userLbl = new Label("👤 " + authService.getCurrentUser());
        userLbl.setStyle("-fx-text-fill:#aaa;");
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("Login.fxml"));
                stage.setScene(new Scene(loader.load(), 500, 400));
                stage.setTitle("Trading System - Login");
                stage.centerOnScreen();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topBar = new HBox(12, title, sp, userLbl, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.setStyle("-fx-background-color:#1a1a2e;");

        // Status bar
        statusLabel = new Label("Ready — enter a stock symbol and click Search");
        statusLabel.setStyle("-fx-text-fill:#555;");
        Label hint = new Label("Demo: AAPL · MSFT · GOOGL · TSLA · AMZN · NVDA");
        hint.setStyle("-fx-text-fill:#999;-fx-font-size:11px;");
        Region sbSp = new Region(); HBox.setHgrow(sbSp, Priority.ALWAYS);
        HBox statusBar = new HBox(8, statusLabel, sbSp, hint);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color:#f0f0f0;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildAnalysisTab(), buildCompareTab(), buildPerformanceTab(), buildAlertsTab(), buildAccountTab());

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabs);
        root.setBottom(statusBar);
        return new Scene(root);
    }

    // ── Tab 1 : Stock Analysis ───────────────────────────────────────────────

    private Tab buildAnalysisTab() {
        Tab tab = new Tab("📊  Stock Analysis");

        TextField symField = new TextField();
        symField.setPromptText("e.g. AAPL");
        symField.setPrefWidth(120); symField.setPrefHeight(32);
        DatePicker fromPick = new DatePicker(LocalDate.now().minusDays(90));
        DatePicker toPick   = new DatePicker(LocalDate.now());
        Button searchBtn = new Button("🔍 Search");
        searchBtn.setPrefHeight(32);
        searchBtn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");
        ProgressIndicator spin = new ProgressIndicator();
        spin.setPrefSize(24, 24); spin.setVisible(false);

        HBox toolbar = new HBox(10,
                new Label("Symbol:"), symField,
                new Label("From:"), fromPick,
                new Label("To:"), toPick,
                searchBtn, spin);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color:#fafafa;-fx-border-color:#e0e0e0;-fx-border-width:0 0 1 0;");

        // PriceTickerPanel COMPOUND COMPONENT — reusable, passive view
        PriceTickerPanel tickerPanel = new PriceTickerPanel();

        // Chart
        CategoryAxis xAxis = new CategoryAxis(); xAxis.setLabel("Date");
        NumberAxis   yAxis = new NumberAxis();   yAxis.setLabel("Price (USD)");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false); chart.setCreateSymbols(false); chart.setLegendVisible(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        // Table
        ObservableList<SharePrice> data = FXCollections.observableArrayList();
        TableView<SharePrice> table = buildOhlcvTable(data);
        table.setPrefHeight(170);

        VBox center = new VBox(tickerPanel, chart, table);
        VBox.setVgrow(chart, Priority.ALWAYS);
        BorderPane content = new BorderPane();
        content.setTop(toolbar); content.setCenter(center);

        // Search action
        Runnable doSearch = () -> {
            String sym = symField.getText().trim().toUpperCase();
            if (sym.isEmpty()) { setStatus("Please enter a symbol."); return; }
            LocalDate from = fromPick.getValue() != null ? fromPick.getValue() : LocalDate.now().minusDays(90);
            LocalDate to   = toPick.getValue()   != null ? toPick.getValue()   : LocalDate.now();

            spin.setVisible(true); searchBtn.setDisable(true);
            setStatus("Loading " + sym + "…");

            Task<List<SharePrice>> task = new Task<>() {
                @Override protected List<SharePrice> call() {
                    return priceService.getSharePrices(new ShareQuery(sym, from, to));
                }
            };
            task.setOnSucceeded(ev -> {
                spin.setVisible(false); searchBtn.setDisable(false);
                List<SharePrice> prices = task.getValue();
                if (prices == null || prices.isEmpty()) { setStatus("No data for " + sym); return; }
                prices.sort((a, b) -> a.getDate().compareTo(b.getDate()));
                SharePrice first = prices.get(0), last = prices.get(prices.size() - 1);
                double chg = last.getClosePriceAsDouble() - first.getClosePriceAsDouble();
                double pct = first.getClosePriceAsDouble() != 0 ? (chg / first.getClosePriceAsDouble()) * 100 : 0;
                double hi  = prices.stream().mapToDouble(SharePrice::getHighPriceAsDouble).max().orElse(0);
                double lo  = prices.stream().mapToDouble(SharePrice::getLowPriceAsDouble).min().orElse(0);
                long   tv  = prices.stream().mapToLong(SharePrice::getVolume).sum();
                // Update the PriceTickerPanel compound component
                tickerPanel.update(last, first);
                // Draw chart
                chart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(sym);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
                int step = Math.max(1, prices.size() / 60);
                for (int i = 0; i < prices.size(); i += step)
                    series.getData().add(new XYChart.Data<>(
                            prices.get(i).getDate().format(fmt),
                            prices.get(i).getClosePriceAsDouble()));
                chart.getData().add(series);
                // Fill table
                data.setAll(prices);
                setStatus(String.format("Loaded %d trading days for %s", prices.size(), sym));
            });
            task.setOnFailed(ev -> {
                spin.setVisible(false); searchBtn.setDisable(false);
                setStatus("Error: " + task.getException().getMessage());
            });
            new Thread(task, "search").start();
        };

        searchBtn.setOnAction(e -> doSearch.run());
        symField.setOnAction(e -> doSearch.run());
        tab.setContent(content);
        return tab;
    }

    // ── Tab 2 : Compare Stocks ───────────────────────────────────────────────

    private Tab buildCompareTab() {
        Tab tab = new Tab("⚖️  Compare Stocks");

        TextField sym1 = new TextField("AAPL"); sym1.setPrefWidth(90); sym1.setPrefHeight(32);
        TextField sym2 = new TextField("MSFT"); sym2.setPrefWidth(90); sym2.setPrefHeight(32);
        DatePicker from = new DatePicker(LocalDate.now().minusDays(90));
        DatePicker to   = new DatePicker(LocalDate.now());
        Button btn = new Button("⚖️ Compare"); btn.setPrefHeight(32);
        btn.setStyle("-fx-background-color:#6a1b9a;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");
        ProgressIndicator spin = new ProgressIndicator(); spin.setPrefSize(24, 24); spin.setVisible(false);

        HBox inputRow = new HBox(10,
                new Label("Symbol 1:"), sym1, new Label("vs"), sym2,
                new Label("From:"), from, new Label("To:"), to, btn, spin);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(10, 12, 10, 12));
        inputRow.setStyle("-fx-background-color:#fafafa;-fx-border-color:#e0e0e0;-fx-border-width:0 0 1 0;");

        CategoryAxis cx = new CategoryAxis(); cx.setLabel("Date");
        NumberAxis   cy = new NumberAxis();   cy.setLabel("Indexed Return (base = 100)");
        LineChart<String, Number> chart = new LineChart<>(cx, cy);
        chart.setAnimated(false); chart.setCreateSymbols(false);
        chart.setTitle("Normalised Performance Comparison");
        VBox.setVgrow(chart, Priority.ALWAYS);

        TableView<String[]> summaryTable = buildSummaryTable();
        summaryTable.setPrefHeight(95);
        Label cStatus = new Label("Enter two symbols and click Compare.");
        cStatus.setPadding(new Insets(4, 8, 4, 8));
        cStatus.setStyle("-fx-text-fill:#666;");

        VBox content = new VBox(inputRow, chart, summaryTable, cStatus);
        VBox.setVgrow(chart, Priority.ALWAYS);

        btn.setOnAction(e -> {
            String s1 = sym1.getText().trim().toUpperCase();
            String s2 = sym2.getText().trim().toUpperCase();
            if (s1.isEmpty() || s2.isEmpty()) { cStatus.setText("Enter both symbols."); return; }
            LocalDate f = from.getValue() != null ? from.getValue() : LocalDate.now().minusDays(90);
            LocalDate t = to.getValue()   != null ? to.getValue()   : LocalDate.now();
            btn.setDisable(true); spin.setVisible(true);
            cStatus.setText("Loading " + s1 + " and " + s2 + "…");

            Task<List<List<SharePrice>>> task = new Task<>() {
                @Override protected List<List<SharePrice>> call() {
                    return List.of(
                            priceService.getSharePrices(new ShareQuery(s1, f, t)),
                            priceService.getSharePrices(new ShareQuery(s2, f, t)));
                }
            };
            task.setOnSucceeded(ev -> {
                btn.setDisable(false); spin.setVisible(false);
                List<SharePrice> p1 = task.getValue().get(0);
                List<SharePrice> p2 = task.getValue().get(1);
                if (p1.isEmpty() || p2.isEmpty()) { cStatus.setText("No data for one or both symbols."); return; }
                chart.getData().clear();
                chart.getData().addAll(normSeries(s1, p1), normSeries(s2, p2));
                summaryTable.getItems().setAll(summaryRow(s1, p1), summaryRow(s2, p2));
                cStatus.setText(String.format("Comparing %s (%d days) vs %s (%d days)", s1, p1.size(), s2, p2.size()));
                setStatus("Comparison: " + s1 + " vs " + s2);
            });
            task.setOnFailed(ev -> { btn.setDisable(false); spin.setVisible(false);
                cStatus.setText("Error: " + task.getException().getMessage()); });
            new Thread(task, "compare").start();
        });

        tab.setContent(content);
        return tab;
    }

    // ── Tab 3 : Performance Report ───────────────────────────────────────────

    private Tab buildPerformanceTab() {
        Tab tab = new Tab("📈  Performance");

        TextField symField = new TextField(); symField.setPromptText("e.g. AAPL"); symField.setPrefWidth(120); symField.setPrefHeight(32);
        DatePicker fromPick = new DatePicker(LocalDate.now().minusDays(90));
        DatePicker toPick   = new DatePicker(LocalDate.now());
        Button btn = new Button("Analyse"); btn.setPrefHeight(32);
        btn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");

        HBox toolbar = new HBox(10, new Label("Symbol:"), symField, new Label("From:"), fromPick, new Label("To:"), toPick, btn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color:#fafafa;-fx-border-color:#e0e0e0;-fx-border-width:0 0 1 0;");

        TextArea resultArea = new TextArea("Run an analysis to see results here.");
        resultArea.setEditable(false);
        resultArea.setStyle("-fx-font-family:monospace;-fx-font-size:13px;");
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        VBox content = new VBox(toolbar, resultArea);
        VBox.setVgrow(resultArea, Priority.ALWAYS);
        content.setPadding(new Insets(0));

        btn.setOnAction(e -> {
            String sym = symField.getText().trim().toUpperCase();
            if (sym.isEmpty()) { resultArea.setText("Please enter a symbol."); return; }
            LocalDate f = fromPick.getValue() != null ? fromPick.getValue() : LocalDate.now().minusDays(90);
            LocalDate t = toPick.getValue()   != null ? toPick.getValue()   : LocalDate.now();
            try {
                java.util.Map<String, Object> summary = perfService.getSummary(sym, f, t);
                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════\n");
                sb.append("  Performance Report: ").append(sym).append("\n");
                sb.append("═══════════════════════════════════\n\n");
                sb.append(String.format("  Period:       %s  →  %s%n", f, t));
                sb.append(String.format("  Start Price:  $%.2f%n", toDouble(summary.get("startPrice"))));
                sb.append(String.format("  End Price:    $%.2f%n", toDouble(summary.get("endPrice"))));
                sb.append(String.format("  Period High:  $%.2f%n", toDouble(summary.get("highPrice"))));
                sb.append(String.format("  Period Low:   $%.2f%n", toDouble(summary.get("lowPrice"))));
                sb.append(String.format("  Total Return: $%.2f%n", toDouble(summary.get("totalReturn"))));
                sb.append(String.format("  %% Return:     %.2f%%%n", toDouble(summary.get("percentageReturn"))));
                resultArea.setText(sb.toString());
                setStatus("Performance report generated for " + sym);
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        });

        tab.setContent(content);
        return tab;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TableView<SharePrice> buildOhlcvTable(ObservableList<SharePrice> data) {
        TableView<SharePrice> t = new TableView<>(data);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Search for a stock to see OHLCV data"));
        t.getColumns().addAll(
            col("Date",   p -> p.getDate().toString()),
            col("Open",   p -> String.format("$%.2f", p.getOpenPriceAsDouble())),
            col("High",   p -> String.format("$%.2f", p.getHighPriceAsDouble())),
            col("Low",    p -> String.format("$%.2f", p.getLowPriceAsDouble())),
            col("Close",  p -> String.format("$%.2f", p.getClosePriceAsDouble())),
            col("Volume", p -> String.format("%,d", p.getVolume()))
        );
        return t;
    }

    @FunctionalInterface interface Ex { String get(SharePrice p); }
    @SuppressWarnings("unchecked")
    private TableColumn<SharePrice, String> col(String h, Ex ex) {
        TableColumn<SharePrice, String> c = new TableColumn<>(h);
        c.setCellValueFactory(cd -> new SimpleStringProperty(ex.get(cd.getValue())));
        return c;
    }

    private TableView<String[]> buildSummaryTable() {
        TableView<String[]> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Run a comparison to see results"));
        String[] headers = {"Symbol","Start","End","Change","% Change","High","Low","Avg Volume"};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> c = new TableColumn<>(headers[i]);
            c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[idx]));
            t.getColumns().add(c);
        }
        return t;
    }

    private XYChart.Series<String, Number> normSeries(String name, List<SharePrice> prices) {
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName(name);
        if (prices.isEmpty()) return s;
        prices.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        double base = prices.get(0).getClosePriceAsDouble(); if (base == 0) base = 1;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        int step = Math.max(1, prices.size() / 60);
        final double b = base;
        for (int i = 0; i < prices.size(); i += step)
            s.getData().add(new XYChart.Data<>(prices.get(i).getDate().format(fmt),
                    (prices.get(i).getClosePriceAsDouble() / b) * 100.0));
        return s;
    }

    private String[] summaryRow(String sym, List<SharePrice> p) {
        double start = p.get(0).getClosePriceAsDouble(), end = p.get(p.size()-1).getClosePriceAsDouble();
        double chg = end - start, pct = start != 0 ? (chg/start)*100 : 0;
        double hi = p.stream().mapToDouble(SharePrice::getHighPriceAsDouble).max().orElse(0);
        double lo = p.stream().mapToDouble(SharePrice::getLowPriceAsDouble).min().orElse(0);
        long av = (long) p.stream().mapToLong(SharePrice::getVolume).average().orElse(0);
        return new String[]{sym, String.format("$%.2f",start), String.format("$%.2f",end),
                String.format("%s$%.2f",chg>=0?"+":"",chg), String.format("%.2f%%",pct),
                String.format("$%.2f",hi), String.format("$%.2f",lo), String.format("%,d",av)};
    }

    private double toDouble(Object o) {
        if (o instanceof java.math.BigDecimal) return ((java.math.BigDecimal) o).doubleValue();
        if (o instanceof Double)               return (Double) o;
        return 0.0;
    }

    private void setStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    // ── Tab 4 : Alerts ───────────────────────────────────────────────────────

    private Tab buildAlertsTab() {
        Tab tab = new Tab("🔔  Alerts");

        // Use AlertPanel COMPOUND COMPONENT — reusable, self-contained
        AlertPanel alertPanel = new AlertPanel(alertService);
        VBox.setVgrow(alertPanel, Priority.ALWAYS);

        VBox content = new VBox(alertPanel);
        VBox.setVgrow(alertPanel, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

        // ── Tab 5 : Account ──────────────────────────────────────────────────────

    private Tab buildAccountTab() {
        Tab tab = new Tab("👤  Account");

        com.example.trading.model.Account acct = walletService.getCurrentUserAccount();

        // ── Info card ──────────────────────────────────────────────────────
        Label idVal       = new Label(acct.getAccountId());
        Label userVal     = new Label(acct.getUsername());
        Label balanceVal  = new Label(formatBalance(acct.getBalance()));
        Label createdVal  = new Label(acct.getCreatedAt() != null ? acct.getCreatedAt().toLocalDate().toString() : "—");
        Label watchVal    = new Label(String.valueOf(acct.getWatchlist().size()) + " symbols");
        Label summaryVal  = new Label(walletService.getAccountSummary());

        idVal.setStyle("-fx-font-size:12px;-fx-text-fill:#1a73e8;-fx-font-family:monospace;");
        userVal.setStyle("-fx-font-size:13px;-fx-font-weight:bold;");
        balanceVal.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#2e7d32;");
        createdVal.setStyle("-fx-font-size:13px;-fx-text-fill:#555;");
        watchVal.setStyle("-fx-font-size:13px;");
        summaryVal.setStyle("-fx-font-size:11px;-fx-text-fill:#888;-fx-font-family:monospace;");
        summaryVal.setWrapText(true);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20); infoGrid.setVgap(12);
        infoGrid.setPadding(new Insets(20, 24, 20, 24));
        infoGrid.setStyle("-fx-background-color:#f8f9fa;-fx-background-radius:8;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-border-width:1;");
        addInfoRow(infoGrid, 0, "Account ID:",   idVal);
        addInfoRow(infoGrid, 1, "Username:",      userVal);
        addInfoRow(infoGrid, 2, "Balance:",       balanceVal);
        addInfoRow(infoGrid, 3, "Member Since:",  createdVal);
        addInfoRow(infoGrid, 4, "Watchlist:",     watchVal);
        addInfoRow(infoGrid, 5, "Summary:",       summaryVal);

        // ── Wallet operations ──────────────────────────────────────────────
        Label walletTitle = new Label("Wallet Operations");
        walletTitle.setStyle("-fx-font-size:14px;-fx-font-weight:bold;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g. 500.00)");
        amountField.setPrefWidth(160); amountField.setPrefHeight(32);

        Button depositBtn = new Button("＋ Deposit");
        depositBtn.setPrefHeight(32);
        depositBtn.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");

        Button withdrawBtn = new Button("－ Withdraw");
        withdrawBtn.setPrefHeight(32);
        withdrawBtn.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");

        Label txStatus = new Label("");
        txStatus.setStyle("-fx-font-size:12px;");

        HBox walletRow = new HBox(10, new Label("Amount: $"), amountField, depositBtn, withdrawBtn, txStatus);
        walletRow.setAlignment(Pos.CENTER_LEFT);
        walletRow.setPadding(new Insets(14, 0, 14, 0));

        // ── Transfer ───────────────────────────────────────────────────────
        Label transferTitle = new Label("Transfer Funds");
        transferTitle.setStyle("-fx-font-size:14px;-fx-font-weight:bold;");

        TextField recipientField = new TextField();
        recipientField.setPromptText("Recipient username");
        recipientField.setPrefWidth(160); recipientField.setPrefHeight(32);

        TextField transferAmtField = new TextField();
        transferAmtField.setPromptText("Amount");
        transferAmtField.setPrefWidth(120); transferAmtField.setPrefHeight(32);

        Button transferBtn = new Button("➜ Transfer");
        transferBtn.setPrefHeight(32);
        transferBtn.setStyle("-fx-background-color:#6a1b9a;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:5;");

        Label transferStatus = new Label("");
        transferStatus.setStyle("-fx-font-size:12px;");

        HBox transferRow = new HBox(10, new Label("To:"), recipientField, new Label("Amount: $"), transferAmtField, transferBtn, transferStatus);
        transferRow.setAlignment(Pos.CENTER_LEFT);
        transferRow.setPadding(new Insets(14, 0, 14, 0));

        // ── Watchlist ─────────────────────────────────────────────────────
        Label watchlistTitle = new Label("Watchlist");
        watchlistTitle.setStyle("-fx-font-size:14px;-fx-font-weight:bold;");

        ObservableList<String> watchlistData = FXCollections.observableArrayList(walletService.getWatchlist());
        ListView<String> watchlistView = new ListView<>(watchlistData);
        watchlistView.setPrefHeight(150);
        watchlistView.setPlaceholder(new Label("No symbols in watchlist."));

        TextField watchField = new TextField();
        watchField.setPromptText("Symbol (e.g. TSLA)");
        watchField.setPrefWidth(130); watchField.setPrefHeight(30);

        Button addWatchBtn    = new Button("＋ Add");
        Button removeWatchBtn = new Button("✕ Remove");
        addWatchBtn.setPrefHeight(30);
        removeWatchBtn.setPrefHeight(30);
        addWatchBtn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;-fx-background-radius:5;");
        removeWatchBtn.setStyle("-fx-background-color:#757575;-fx-text-fill:white;-fx-background-radius:5;");

        HBox watchControls = new HBox(8, watchField, addWatchBtn, removeWatchBtn);
        watchControls.setAlignment(Pos.CENTER_LEFT);
        watchControls.setPadding(new Insets(8, 0, 0, 0));

        // ── Layout ─────────────────────────────────────────────────────────
        VBox content = new VBox(16,
                infoGrid,
                new Separator(),
                walletTitle, walletRow,
                new Separator(),
                transferTitle, transferRow,
                new Separator(),
                watchlistTitle, watchlistView, watchControls);
        content.setPadding(new Insets(20, 24, 20, 24));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;");

        // ── Deposit ────────────────────────────────────────────────────────
        depositBtn.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                BigDecimal newBal = walletService.deposit(amount);
                balanceVal.setText(formatBalance(newBal));
                summaryVal.setText(walletService.getAccountSummary());
                txStatus.setText("✅ Deposited " + formatBalance(amount));
                txStatus.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:12px;");
                amountField.clear();
                setStatus("Deposited " + formatBalance(amount));
            } catch (Exception ex) {
                txStatus.setText("❌ " + ex.getMessage());
                txStatus.setStyle("-fx-text-fill:#e53935;-fx-font-size:12px;");
            }
        });

        // ── Withdraw ───────────────────────────────────────────────────────
        withdrawBtn.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                BigDecimal newBal = walletService.withdraw(amount);
                balanceVal.setText(formatBalance(newBal));
                summaryVal.setText(walletService.getAccountSummary());
                txStatus.setText("✅ Withdrew " + formatBalance(amount));
                txStatus.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:12px;");
                amountField.clear();
                setStatus("Withdrew " + formatBalance(amount));
            } catch (Exception ex) {
                txStatus.setText("❌ " + ex.getMessage());
                txStatus.setStyle("-fx-text-fill:#e53935;-fx-font-size:12px;");
            }
        });

        // ── Transfer ───────────────────────────────────────────────────────
        transferBtn.setOnAction(e -> {
            try {
                String recipient = recipientField.getText().trim();
                BigDecimal amount = new BigDecimal(transferAmtField.getText().trim());
                walletService.transfer(recipient, amount);
                balanceVal.setText(formatBalance(walletService.getBalance()));
                summaryVal.setText(walletService.getAccountSummary());
                transferStatus.setText("✅ Sent " + formatBalance(amount) + " to " + recipient);
                transferStatus.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:12px;");
                recipientField.clear(); transferAmtField.clear();
                setStatus("Transferred " + formatBalance(amount) + " to " + recipient);
            } catch (Exception ex) {
                transferStatus.setText("❌ " + ex.getMessage());
                transferStatus.setStyle("-fx-text-fill:#e53935;-fx-font-size:12px;");
            }
        });

        // ── Watchlist ──────────────────────────────────────────────────────
        addWatchBtn.setOnAction(e -> {
            String sym = watchField.getText().trim().toUpperCase();
            if (sym.isEmpty()) return;
            walletService.addToWatchlist(sym);
            watchlistData.setAll(walletService.getWatchlist());
            watchVal.setText(watchlistData.size() + " symbols");
            summaryVal.setText(walletService.getAccountSummary());
            watchField.clear();
            setStatus(sym + " added to watchlist.");
        });
        watchField.setOnAction(e -> addWatchBtn.fire());

        removeWatchBtn.setOnAction(e -> {
            String selected = watchlistView.getSelectionModel().getSelectedItem();
            if (selected == null) { setStatus("Select a symbol to remove."); return; }
            walletService.removeFromWatchlist(selected);
            watchlistData.setAll(walletService.getWatchlist());
            watchVal.setText(watchlistData.size() + " symbols");
            summaryVal.setText(walletService.getAccountSummary());
            setStatus(selected + " removed from watchlist.");
        });

        tab.setContent(scroll);
        return tab;
    }


    private void addInfoRow(GridPane grid, int row, String label, javafx.scene.Node value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#888;-fx-font-weight:bold;");
        grid.add(lbl,   0, row);
        grid.add(value, 1, row);
    }

    private String formatBalance(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount.doubleValue());
    }

}
