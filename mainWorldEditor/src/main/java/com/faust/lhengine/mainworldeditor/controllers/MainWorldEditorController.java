package com.faust.lhengine.mainworldeditor.controllers;

import com.esotericsoftware.jsonbeans.Json;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.mainworldeditor.enums.MainWorldEditorScenes;
import com.faust.lhengine.mainworldeditor.model.MainWorldData;
import com.faust.lhengine.mainworldeditor.serialization.MainWorldSerializer;
import com.faust.lhengine.utils.Pair;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main World Editor Controller
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MainWorldEditorController extends AbstractController {

    private MainWorldData mainWorldData;
    private Pair<Integer,Integer> worldLimit;
    private boolean isBoundarySelectionModeOn = false;
    private Pair<DirectionEnum,RoomPosition> boundarySelectionModeDestination = null;

    @FXML
    protected ScrollPane roomBoxContainer;

    private RoomBoxController boundarySelectionEventSource;

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
    protected void closeCurrentMainWorld() {
        mainWorldData = null;
        roomBoxContainer.setContent(null);
    }

    @FXML
    protected void saveCurrentMainWorld() throws FileNotFoundException {

        //Open file chooser with save
        final var fileChooser = new FileChooser();
        final Stage stage = (Stage) rootVbox.getScene().getWindow();

        //Set extension filter for text files
        var extFilter = new FileChooser.ExtensionFilter("Json file (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        var file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveMapToFile(file);
        }
    }

    /**
     * Create a new mainWorld given width and height. All rooms will be EMPTY_SPACE
     *
     * @param width
     * @param height
     * @throws IOException
     */
    public void createNewWorld(int width, int height) throws IOException {

        worldLimit = new Pair<>(width,height);
        mainWorldData = new MainWorldData();

        // add the correct number of room boxes
        for(int x=0; x < width; x++){
            for(int y=0; y < height; y++){
                mainWorldData.terrains.put(new RoomPosition(x,y),new RoomModel(new HashMap<>(),RoomTypeEnum.EMPTY_SPACE));
            }
        }

        populateRoomBoxContainer();
    }

    /**
     * Sets a new type for roomPosition. Clears all previously set boundaries
     * @param roomPosition
     * @param newType
     */
    public void setNewRoomType(RoomPosition roomPosition, RoomTypeEnum newType) {
        System.out.println("New room of type " + newType.name() + " at " + roomPosition);
        mainWorldData.terrains.put(roomPosition, new RoomModel(new HashMap<>(),newType));
    }

    private void saveMapToFile(File file) throws FileNotFoundException {

        var writer = new PrintWriter(file);
        writer.println(mainWorldData.toJson());
        writer.close();
    }

    @FXML
    protected void loadMainWorldFromFile() throws IOException {

        //Open file chooser with save
        final var fileChooser = new FileChooser();
        final Stage stage = (Stage) rootVbox.getScene().getWindow();

        //Set extension filter for text files
        var extFilter = new FileChooser.ExtensionFilter("Json file (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        var file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            loadMapFromFile(file);
        }
    }

    /**
     *
     * @param file
     * @throws IOException
     */
    public void loadMapFromFile(File file) throws IOException {
        Json jsonParser = new Json();
        jsonParser.setSerializer(MainWorldData.class, new MainWorldSerializer());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            mainWorldData = jsonParser.fromJson(MainWorldData.class,reader.lines().collect(Collectors.joining("\n")));

            //Calculate width and height of the map
            int width = 0;
            int height =0;

            for(Map.Entry<RoomPosition, RoomModel> t : mainWorldData.terrains.entrySet()){
                width = Math.max(width,t.getKey().getX());
                height = Math.max(height,t.getKey().getY());
            }

            worldLimit = new Pair<>(width+1,height+1);

            populateRoomBoxContainer();

        }
    }

    /**
     * Populate the UI with the roomboxes for the map
     *
     * @throws IOException
     */
    private void populateRoomBoxContainer() throws IOException {

        //Creates a GridPane with a RoomBox in each cell
        final GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        FXMLLoader loader;
        for(Map.Entry<RoomPosition,RoomModel> entry: mainWorldData.terrains.entrySet()){
            loader = new FXMLLoader(getClass().getResource(MainWorldEditorScenes.ROOM_BOX.getFilename()));
            Node roomBox = loader.load();
            RoomBoxController controller = loader.getController();
            controller.setRoomData(entry);

            //Use Height - y for rendering to respect the bottom-left origin of the mainworld IN GAME
            gridPane.add(roomBox,entry.getKey().getX(),worldLimit.getSecond()- entry.getKey().getY());
        }

        roomBoxContainer.setContent(gridPane);
        roomBoxContainer.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.
    }
    /**
     * start BoundarySelectionMode
     *
     * @param directionEnum
     * @param roomPosition
     * @param eventSource
     */
    public void startBoundarySelection(DirectionEnum directionEnum, RoomPosition roomPosition, RoomBoxController eventSource) {

        isBoundarySelectionModeOn = true;
        boundarySelectionModeDestination = new Pair<>(directionEnum,roomPosition);
        boundarySelectionEventSource = eventSource;

    }

    /**
     * Select boundary if BoundarySelectionMode is on
     *
     * @param boundaryDestination
     */
    public void selectBoundary(RoomPosition boundaryDestination) {

        //If in BoundarySelectionMode
        if(isBoundarySelectionModeOn){

            Objects.requireNonNull(boundarySelectionModeDestination);

            RoomPosition position = boundarySelectionModeDestination.getSecond();
            DirectionEnum direction = boundarySelectionModeDestination.getFirst();

            //inject new boundary
            mainWorldData.terrains.get(position).boundaries.put(direction,boundaryDestination);

            //Close if null
            if(Objects.nonNull(boundaryDestination))
                boundarySelectionEventSource.setButtonText(boundaryDestination.toString());
            else
                boundarySelectionEventSource.setButtonText("X");

            boundarySelectionEventSource.clearBoundaryTempVariables();
            exitBoundarySelectionMode();
        }
    }

    /**
     *
     */
    public void exitBoundarySelectionMode() {
        //Remove references and turn off BoundarySelection Mode
        isBoundarySelectionModeOn = false;
        boundarySelectionModeDestination = null;
        boundarySelectionEventSource = null;
    }
}