package com.faust.lhengine.mainworldeditor.controllers;

import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.mainworldeditor.mediator.ControllerMediator;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.*;

/**
 * Main World Editor Controller
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class RoomBoxController extends AbstractController implements Initializable {

    private RoomModel roomModel;
    private RoomPosition roomPosition;

    @FXML
    private Button upButton;

    @FXML
    private Button rightButton;

    @FXML
    private Button downButton;

    @FXML
    private Button leftButton;

    @FXML
    private Button closeBoundaryButton;

    @FXML
    private Button exitBoundaryButton;
    private String temporaryTextPlaceholder;


    @FXML
    private ComboBox<RoomTypeEnum> terrainTypesCombobox;

    private Button boundaryDestination;

    @FXML
    protected void onChangeTerrainType(Event event) {
        ControllerMediator.getInstance().mainWorldEditorControllerSetNewRoomType(roomPosition, terrainTypesCombobox.getValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (terrainTypesCombobox.getItems().isEmpty()) {
            terrainTypesCombobox.setItems(FXCollections.observableList(List.of(RoomTypeEnum.values())));
        }

        terrainTypesCombobox.getSelectionModel().select(RoomTypeEnum.EMPTY_SPACE);

    }

    /**
     * Populate room box editable fields
     *
     * @param entry
     */
    public void setRoomData(Map.Entry<RoomPosition, RoomModel> entry) {

        roomModel = entry.getValue();
        roomPosition = entry.getKey();
        terrainTypesCombobox.getSelectionModel().select(roomModel.type);

        for(Map.Entry<DirectionEnum, RoomPosition> b: roomModel.boundaries.entrySet()){

            if(Objects.isNull(b.getValue())){
                initalizeButtonText(b.getKey(),"X");
            } else {
                initalizeButtonText(b.getKey(),b.getValue().toString());
            }

        }

    }

    @FXML
    protected void startBoundarySelection(Event event){

        //if another boundary was selected before, handle it
        if(Objects.nonNull(boundaryDestination)){
            boundaryDestination.setText(temporaryTextPlaceholder);
        }

        //Extract data from button and send it to editor
        boundaryDestination = ((Button) event.getSource());
        String directionEnumData = (String) boundaryDestination.getUserData();

        temporaryTextPlaceholder = boundaryDestination.getText();
        boundaryDestination.setText("...");

        DirectionEnum directionEnum = DirectionEnum.valueOf(directionEnumData);

        ControllerMediator.getInstance().mainWorldEditorControllerStartBoundarySelection(directionEnum,roomPosition, this);

        closeBoundaryButton.setDisable(false);
        exitBoundaryButton.setDisable(false);
    }

    /**
     *
     */
    @FXML
    protected void selectBoundary(){

        ControllerMediator.getInstance().mainWorldEditorControllerSelectBoundary(roomPosition);
        clearBoundaryTempVariables();
    }

    /**
     *
     */
    @FXML
    protected void closeBoundary(){

        ControllerMediator.getInstance().mainWorldEditorControllerSelectBoundary(null);
        clearBoundaryTempVariables();
    }

    /**
     *
     */
    @FXML
    protected void exitBoundarySelectionMode(){

        boundaryDestination.setText(temporaryTextPlaceholder);
        ControllerMediator.getInstance().mainWorldEditorControllerExitBoundarySelectionMode();
        clearBoundaryTempVariables();
    }

    /**
     *
     */
    public void clearBoundaryTempVariables(){
        closeBoundaryButton.setDisable(true);
        exitBoundaryButton.setDisable(true);
        boundaryDestination = null;
        temporaryTextPlaceholder= null;
    }

    /**
     *
     * @param newText
     */
    public void setButtonText(String newText) {
        boundaryDestination.setText(newText);
    }

    /**
     *
     * @param key
     * @param text
     */
    private void initalizeButtonText(DirectionEnum key, String text) {
        switch (key){

            case UP: {
                upButton.setText(text);
                break;
            }
            case RIGHT: {
                rightButton.setText(text);
                break;
            }
            case DOWN:{
                downButton.setText(text);
                break;
            }
            case LEFT: {
                leftButton.setText(text);
                break;
            }
        }
    }

}
