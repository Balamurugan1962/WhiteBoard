module com.figma.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.figma.core to javafx.fxml;
    exports com.figma.core;
//    opens com.figma.core to javafx.fxml;
}