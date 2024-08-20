package com.faust.lhengine.mainworldeditor.controllers;

import com.faust.lhengine.mainworldeditor.enums.MainWorldEditorScenes;
import com.faust.lhengine.mainworldeditor.mediator.ControllerMediator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Abstract Controller
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class AbstractController {

    @FXML
    protected Parent rootVbox;
    protected String uuid;

    public AbstractController() {
        ControllerMediator.getInstance().registerController(this);
    }

    /**
     * Change current Scene
     *
     * @param newScreen MainWorldEditorScenes value of the new screen
     * @throws IOException
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    public void changeScene (MainWorldEditorScenes newScreen) throws IOException {
        Objects.requireNonNull(newScreen);
        Objects.requireNonNull(getClass().getResource(newScreen.getFilename()));

        URL newScreenURL = getClass().getResource(newScreen.getFilename());
        final Parent newSceneRoot = FXMLLoader.load(newScreenURL);

        final Stage stage = (Stage) rootVbox.getScene().getWindow();
        stage.setScene(new Scene(newSceneRoot));
        stage.show();
    }

    /**
     * Opens a modal popup
     *
     * @param modalUserData JavaFX node userData, which must contain a valid MainWorldEditorScenes enum as string
     * @throws IOException
     */
    protected void openModalPopup(String modalUserData) throws IOException {
        Objects.requireNonNull(modalUserData);

        final String filename = MainWorldEditorScenes.valueOf(modalUserData).getFilename();

        final Stage openedModal = new Stage();
        openedModal.initModality(Modality.APPLICATION_MODAL);
        openedModal.initOwner(rootVbox.getScene().getWindow());

        URL popupFileUrl = getClass().getResource(filename);
        Objects.requireNonNull(popupFileUrl);

        final Scene dialogScene = new Scene(FXMLLoader.load(popupFileUrl));
        openedModal.setScene(dialogScene);
        openedModal.show();

    }

    /**
     * Closes stage
     */
    @FXML
    protected void closeStage(){
        final Stage stage = (Stage) rootVbox.getScene().getWindow();
        stage.close();
    }

    public String getUuid() {
        return uuid;
    }
}
