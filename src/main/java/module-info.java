module wmediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;

    opens wmediaplayer to javafx.fxml;
    opens controllers to javafx.fxml;
    exports controllers;
    exports wmediaplayer;
}
