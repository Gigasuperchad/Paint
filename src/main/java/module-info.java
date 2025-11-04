module com.example.paintoop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.paintoop to javafx.fxml;
    exports com.example.paintoop;
}