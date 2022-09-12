package com.faust.lhengine.game.rooms.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.game.rooms.AbstractRoom;
import com.faust.lhengine.game.rooms.OnRoomChangeListener;
import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomFlagEnum;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.game.rooms.impl.CasualRoom;
import com.faust.lhengine.game.rooms.impl.FixedRoom;
import com.faust.lhengine.game.splash.SplashManager;
import com.faust.lhengine.game.textbox.manager.TextBoxManager;
import com.faust.lhengine.game.world.manager.WorldManager;
import com.faust.lhengine.saves.RoomSaveEntry;
import com.faust.lhengine.saves.AbstractSaveFileManager;

import java.util.*;

/**
 * Room Manager class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class RoomsManager {

    private final SplashManager splashManager;
    private final AssetManager assetManager;
    private final AbstractSaveFileManager saveFileManager;
    private final List<OnRoomChangeListener> onRoomChangeListeners;

    private AbstractRoom currentRoom;
    private final Vector2 currentRoomPosInWorld = new Vector2(0, 0);

    /**
     * MainWorld Matrix
     */
    private final Map<Vector2, RoomModel> mainWorld = new HashMap<>();
    private final Map<Vector2, RoomSaveEntry> saveMap = new HashMap<>();
    private final Vector2 mainWorldSize = new Vector2(0, 0);

    private final WorldManager worldManager;
    private final TextBoxManager textManager;
    private final PlayerInstance player;
    private final OrthographicCamera camera;
    private final MusicManager musicManager;

    public RoomsManager(WorldManager worldManager, TextBoxManager textManager, SplashManager splashManager, PlayerInstance player, OrthographicCamera camera, AssetManager assetManager, AbstractSaveFileManager saveFileManager, MusicManager musicManager) {
        this.worldManager = worldManager;
        this.textManager = textManager;
        this.splashManager = splashManager;
        this.assetManager = assetManager;
        this.saveFileManager = saveFileManager;
        this.musicManager = musicManager;
        this.player = player;
        this.camera = camera;
        this.onRoomChangeListeners = new ArrayList<>();
        addRoomChangeListener(player);

        initMainWorld();
    }

    /**
     * Inits world from file
     */
    private void initMainWorld() {

        JsonValue terrains = new JsonReader().parse(Gdx.files.internal("mainWorldModel.json")).get("terrains");
        mainWorldSize.set(0, 0);

        terrains.forEach((t) -> {
            Vector2 v = new Vector2(t.getFloat("x"), t.getFloat("y"));
            RoomTypeEnum type = RoomTypeEnum.valueOf(t.getString("roomType"));
            Objects.requireNonNull(type);

            //Parsing boundaries
            JsonValue boundariesJson = t.get("boundaries");
            Map<DirectionEnum, RoomPosition> boundaries = new HashMap<>();
            if (Objects.nonNull(boundariesJson)) {
                boundariesJson.forEach((b) -> {
                    //Parsing targets
                    JsonValue targetJson = b.get("target");
                    //Null if impassable
                    RoomPosition target = null;
                    if (Objects.nonNull(targetJson.child)) {
                        target = new RoomPosition(targetJson.getInt("x"), targetJson.getInt("y"));
                    }
                    DirectionEnum side = DirectionEnum.valueOf(b.getString("side"));
                    Objects.requireNonNull(side);
                    boundaries.put(side, target);
                });
            }

            mainWorld.put(v, new RoomModel(boundaries, type));
            mainWorldSize.set(Math.max(mainWorldSize.x, v.x), Math.max(mainWorldSize.y, v.y));
        });
        // Finalize size
        mainWorldSize.set(mainWorldSize.x + 1, mainWorldSize.y + 1);

        //Try to load predefined casualnumbers for casual rooms from file
        try {
            saveFileManager.loadSaveForGame(player, saveMap);

        } catch (SerializationException ex) {
            Gdx.app.log("WARN", "No valid savefile to load");
        }
        //Init gamefile if no valid one has found
        saveFileManager.saveOnFile(player, saveMap);
    }

    /**
     * Changes the currentRoom
     *
     * @param newRoomPosX relative to the matrix of the world
     * @param newRoomPosY relative to the matrix of the world
     */
    public void changeCurrentRoom(int newRoomPosX, int newRoomPosY) {

        // Notifiy all listeners
        for(OnRoomChangeListener l: onRoomChangeListeners){
            l.onRoomChangeStart(currentRoom);
        }

        //Do stuff while leaving room
        RoomSaveEntry currentRoomSaveEntry = saveMap.get(currentRoomPosInWorld);

        if (Objects.nonNull(currentRoom)) {
            currentRoom.onRoomLeave(currentRoomSaveEntry);
        }

        //Change room position
        float finalX = (newRoomPosX < 0 ? mainWorldSize.x - 1 : (newRoomPosX == mainWorldSize.x ? 0 : newRoomPosX));
        float finalY = (newRoomPosY < 0 ? mainWorldSize.y - 1 : (newRoomPosY == mainWorldSize.y ? 0 : newRoomPosY));

        // Safety check on y
        if (finalY == 8 && finalX != 3) {
            finalY--;
        }

        currentRoomPosInWorld.set(finalX, finalY);

        //get entry from save or create new
        currentRoomSaveEntry = saveMap.get(currentRoomPosInWorld);
        if (Objects.isNull(currentRoomSaveEntry)) {
            currentRoomSaveEntry = new RoomSaveEntry(
                    (int) finalX,
                    (int) finalY,
                    0, populateRoomFlags(), new HashMap<>());
        } else {
            currentRoomSaveEntry.savedFlags.putAll(populateRoomFlags());
        }


        if (mainWorld.get(currentRoomPosInWorld).type == RoomTypeEnum.CASUAL) {
            currentRoom = new CasualRoom(worldManager, textManager, splashManager, player, camera, assetManager, currentRoomSaveEntry, musicManager);
        } else {
            currentRoom = new FixedRoom(mainWorld.get(currentRoomPosInWorld).type, worldManager, textManager, splashManager, player, camera, assetManager, currentRoomSaveEntry, musicManager);
        }


        Gdx.app.log("DEBUG", "ROOM " + (int) currentRoomPosInWorld.x + "," + (int) currentRoomPosInWorld.y);
        //Keep the same state of already visited rooms
        saveMap.put(currentRoomPosInWorld.cpy(), currentRoomSaveEntry);

        // Notifiy all listeners
        for(OnRoomChangeListener l: onRoomChangeListeners){
            l.onRoomChangeEnd(currentRoom);
        }

    }

    /**
     * @return populated map of flags
     */
    private Map<RoomFlagEnum, Boolean> populateRoomFlags() {
        //default map
        Map<RoomFlagEnum, Boolean> newRoomFlags = RoomFlagEnum.generateDefaultRoomFlags();

        if (RoomTypeEnum.CASUAL.equals(mainWorld.get(currentRoomPosInWorld).type)) {
            //If unvisited rooms are less than the number of found crosses to find, guarantee them
            final boolean guaranteedGoldcross = player.getItemQuantityFound(ItemEnum.GOLDCROSS) < 9 &&
                    (mainWorldSize.x * mainWorldSize.y) - 10 <= (saveMap.size() + (9 - player.getItemQuantityFound(ItemEnum.GOLDCROSS)));
            newRoomFlags.put(RoomFlagEnum.GUARANTEED_GOLDCROSS, guaranteedGoldcross);

            //Only three herbs can be found
            final boolean mustNotHaveHerb = player.getItemQuantityFound(ItemEnum.HEALTH_KIT) >= 3 ||
                    saveMap.values().stream().filter(roomSaveEntry -> CasualRoom.BUSH_MAPS.contains(roomSaveEntry.casualNumber)).count() >= 3;
            newRoomFlags.put(RoomFlagEnum.WITHOUT_HERBS, mustNotHaveHerb);

            //If unvisited rooms (priority is on goldcross) are less than the number of found herbs to find, guarantee them
            final boolean guaranteedHerb = !mustNotHaveHerb && !guaranteedGoldcross &&
                    (mainWorldSize.x * mainWorldSize.y) - 13 <= (saveMap.size() + (3 - player.getItemQuantityFound(ItemEnum.HEALTH_KIT)));
            newRoomFlags.put(RoomFlagEnum.GUARANTEED_HERBS, guaranteedHerb);

        } else if (RoomTypeEnum.hasEchoes(mainWorld.get(currentRoomPosInWorld).type)) {

            //If echoes were disabled in this room, disable them
            if (saveMap.containsKey(currentRoomPosInWorld)) {
                RoomSaveEntry entry = saveMap.get(currentRoomPosInWorld);
                newRoomFlags.put(RoomFlagEnum.DISABLED_ECHO, entry.savedFlags.get(RoomFlagEnum.DISABLED_ECHO));
            }

        }

        //Avoid showing more than one time enemy splash
        saveMap.forEach((key, entry) -> {
            if (entry.savedFlags.get(RoomFlagEnum.FIRST_BOUNDED_ENCOUNTERED)) {
                newRoomFlags.put(RoomFlagEnum.FIRST_BOUNDED_ENCOUNTERED, true);
            }
            if (entry.savedFlags.get(RoomFlagEnum.FIRST_STRIX_ENCOUNTERED)) {
                newRoomFlags.put(RoomFlagEnum.FIRST_STRIX_ENCOUNTERED, true);
            }
            if (entry.savedFlags.get(RoomFlagEnum.FIRST_HIVE_ENCOUNTERED)) {
                newRoomFlags.put(RoomFlagEnum.FIRST_HIVE_ENCOUNTERED, true);
            }
        });


        //Only bounded enemies after 15 rooms are visited
        newRoomFlags.put(RoomFlagEnum.GUARDANTEED_BOUNDED, saveMap.size() >= 15);

        //If this is the room visited, there should be no enemies even if they are in map
        newRoomFlags.put(RoomFlagEnum.DISABLED_ENEMIES, saveMap.size() < 2);


        return newRoomFlags;
    }

    /**
     * Wraps the room contents game logic
     *
     * @param stateTime
     */
    public void doRoomContentsLogic(float stateTime) {
        currentRoom.doRoomContentsLogic(stateTime);

        // In final room should never change
        if (RoomTypeEnum.FINAL.equals(currentRoom.getRoomType())) {
            return;
        }

        // After room logic, handle the room change
        Vector2 playerPosition = player.getBody().getPosition();
        player.setStartX(playerPosition.x);
        player.setStartY(playerPosition.y);

        int newXPosInMatrix = (int) getCurrentRoomPosInWorld().x;
        int newYPosInMatrix = (int) getCurrentRoomPosInWorld().y;

        DirectionEnum switchDirection = DirectionEnum.UNUSED;
        // Check for left or right passage
        if (playerPosition.x < AbstractRoom.LEFT_BOUNDARY) {
            switchDirection = DirectionEnum.LEFT;
        } else if ((playerPosition.x > AbstractRoom.RIGHT_BOUNDARY)) {
            switchDirection = DirectionEnum.RIGHT;
        }

        // Check for top or bottom passage
        if (playerPosition.y < AbstractRoom.BOTTOM_BOUNDARY) {
            switchDirection = DirectionEnum.DOWN;
        } else if (playerPosition.y > AbstractRoom.TOP_BOUNDARY) {
            switchDirection = DirectionEnum.UP;
        } else if (playerPosition.y > LHEngine.GAME_HEIGHT * 0.45 &&
                RoomTypeEnum.CHURCH_ENTRANCE.equals(currentRoom.getRoomType())) {
            //FIXME should add door object?
            //Final room
            switchDirection = DirectionEnum.UP;
            player.setStartY(AbstractRoom.BOTTOM_BOUNDARY + 8);
            saveFileManager.saveOnFile(player, saveMap);
        }


        // Adjustments for world extremes, semi pacman effect
        if (!DirectionEnum.UNUSED.equals(switchDirection)) {
            boolean hasBoundary = mainWorld.get(currentRoomPosInWorld).boundaries.containsKey(switchDirection);
            if (hasBoundary) {
                if (Objects.nonNull(mainWorld.get(currentRoomPosInWorld).boundaries.get(switchDirection))) {
                    newXPosInMatrix = mainWorld.get(currentRoomPosInWorld).boundaries.get(switchDirection).getX();
                    newYPosInMatrix = mainWorld.get(currentRoomPosInWorld).boundaries.get(switchDirection).getY();
                }
            }
            switch (switchDirection) {
                case UP: {
                    if (playerPosition.y > AbstractRoom.TOP_BOUNDARY) {
                        player.setStartY(AbstractRoom.BOTTOM_BOUNDARY + 4);
                        if (!hasBoundary) {
                            newYPosInMatrix++;
                        }
                    }
                    break;
                }
                case RIGHT: {
                    if (playerPosition.x > AbstractRoom.RIGHT_BOUNDARY) {
                        player.setStartX(AbstractRoom.LEFT_BOUNDARY + 4);
                        if (!hasBoundary) {
                            newXPosInMatrix++;
                        }
                    }
                    break;
                }
                case LEFT: {
                    if (playerPosition.x < AbstractRoom.LEFT_BOUNDARY) {
                        player.setStartX(AbstractRoom.RIGHT_BOUNDARY - 4);
                        if (!hasBoundary) {
                            newXPosInMatrix--;
                        }
                    }
                    break;
                }
                case DOWN: {
                    if (playerPosition.y < AbstractRoom.BOTTOM_BOUNDARY) {
                        player.setStartY(AbstractRoom.TOP_BOUNDARY - 4);
                        if (!hasBoundary) {
                            newYPosInMatrix--;
                        }
                    }
                    break;
                }
            }

            //Change room and clear nearest poi reference
            if (getCurrentRoomPosInWorld().x != newXPosInMatrix || getCurrentRoomPosInWorld().y != newYPosInMatrix) {
                changeCurrentRoom(newXPosInMatrix, newYPosInMatrix);
                player.cleanReferences();
            }
        }
    }

    public Vector2 getCurrentRoomPosInWorld() {
        return currentRoomPosInWorld;
    }

    /**
     * Dispose current room contents
     */
    public void dispose() {
        saveFileManager.saveOnFile(player, saveMap);
        onRoomChangeListeners.clear();

        currentRoom.dispose();
    }

    public Map<Vector2, RoomSaveEntry> getSaveMap() {
        return saveMap;
    }

    /**
     *
     * @return current room
     */
    public AbstractRoom getCurrentRoom() {
        return currentRoom;
    }

    /**
     *
     * @param listener
     */
    public void addRoomChangeListener(OnRoomChangeListener listener){
        onRoomChangeListeners.add(listener);
    }
}
