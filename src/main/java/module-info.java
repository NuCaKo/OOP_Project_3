module com.group27 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java; // Bunu az önce düzelttik, kalsın.

    opens com.group27 to javafx.fxml;
    // opens com.group27.auth to javafx.fxml;  <-- BURAYI KAPATTIK

    exports com.group27;
}