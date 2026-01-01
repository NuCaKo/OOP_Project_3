module com.group27 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;
    requires itextpdf;
    requires java.desktop;

    opens com.group27 to javafx.fxml;
    opens com.group27.controller to javafx.fxml;

    exports com.group27;
}