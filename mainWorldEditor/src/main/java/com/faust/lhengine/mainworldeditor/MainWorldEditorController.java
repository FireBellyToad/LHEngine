package com.faust.lhengine.mainworldeditor;

import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.mainworldeditor.enums.MainWorldEditorScenes;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Main World Editor Controller
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MainWorldEditorController extends AbstractController {

    @FXML
    private ComboBox<RoomTypeEnum> terrainTypesCombobox;

    private List<RoomModel> mainWorldData;

    @FXML
    protected void openModalPopupFromMenu(Event event) throws IOException {
        String modalUserData = (String) ((MenuItem) event.getSource()).getUserData();
        openModalPopup(modalUserData);
    }

    @FXML
    protected void openModalPopupFromNode(Event event) throws IOException {
        String modalUserData = (String) ((Node) event.getSource()).getUserData();
        openModalPopup(modalUserData);
    }

    @FXML
    protected void closeCurrentMainWorld() throws IOException {
        changeScene(MainWorldEditorScenes.MAIN);
    }

    @FXML
    protected void populateTerrainTypes(){

        if(terrainTypesCombobox.getItems().isEmpty()){
            System.out.println("Load all terrains");
            terrainTypesCombobox.setItems(FXCollections.observableList(List.of(RoomTypeEnum.values())));
        }
    }
}