module com.example.paintoop {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.paintoop to javafx.fxml;
    exports com.example.paintoop;
}