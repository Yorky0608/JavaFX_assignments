module fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens fx to javafx.fxml;
    exports fx;
}
