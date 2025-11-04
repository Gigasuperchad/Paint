module com.example.paintoop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;

    opens com.example.paintoop to javafx.fxml;
    exports com.example.paintoop;
}