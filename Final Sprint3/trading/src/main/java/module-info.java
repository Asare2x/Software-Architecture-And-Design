module com.example.trading {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    // Open all packages to FXML reflective access
    opens com.example.trading              to javafx.fxml;
    opens com.example.trading.api          to javafx.fxml;
    opens com.example.trading.adapter      to javafx.fxml;
    opens com.example.trading.architecture to javafx.fxml;
    opens com.example.trading.blackboard   to javafx.fxml;
    opens com.example.trading.component    to javafx.fxml;
    opens com.example.trading.exception    to javafx.fxml;
    opens com.example.trading.model        to javafx.fxml;
    opens com.example.trading.mvc          to javafx.fxml;
    opens com.example.trading.pipeline     to javafx.fxml;
    opens com.example.trading.repository   to javafx.fxml;
    opens com.example.trading.service      to javafx.fxml;
    opens com.example.trading.soa          to javafx.fxml;

    exports com.example.trading;
    exports com.example.trading.api;
    exports com.example.trading.adapter;
    exports com.example.trading.architecture;
    exports com.example.trading.blackboard;
    exports com.example.trading.component;
    exports com.example.trading.exception;
    exports com.example.trading.model;
    exports com.example.trading.mvc;
    exports com.example.trading.pipeline;
    exports com.example.trading.repository;
    exports com.example.trading.service;
    exports com.example.trading.soa;
}
