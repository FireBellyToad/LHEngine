package com.faust.lhengine.mainworldeditor;

import com.faust.lhengine.mainworldeditor.enums.MainWorldEditorScenes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Main World Editor Application
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MainWorldEditorApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        //Opens up main view
        URL editingPageUrl = getClass().getResource("controllers/" + MainWorldEditorScenes.EDITING.getFilename());
        Objects.requireNonNull(editingPageUrl);
        Scene scene = new Scene(FXMLLoader.load(editingPageUrl));
        stage.setTitle("LH-Engine Main World Editor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}