module com.example.paintoop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    opens com.example.paintoop to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.paintoop;
}