module com.faust.lhengine.mainworldeditor.controllers {
    requires javafx.controls;
    requires javafx.fxml;
    requires core;


    opens com.faust.lhengine.mainworldeditor.controllers to javafx.fxml;
    exports com.faust.lhengine.mainworldeditor.controllers;
    exports com.faust.lhengine.mainworldeditor;
    opens com.faust.lhengine.mainworldeditor to javafx.fxml;
}