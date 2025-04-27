package com.faust.lhengine.game.rooms.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.faust.lhengine.game.instances.impl.POIInstance;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.game.music.enums.TuneEnum;
import com.faust.lhengine.game.rooms.AbstractRoom;
import com.faust.lhengine.game.rooms.enums.MapLayersEnum;
import com.faust.lhengine.game.rooms.enums.RoomFlagEnum;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.game.splash.SplashManager;
import com.faust.lhengine.game.textbox.manager.TextBoxManager;
import com.faust.lhengine.game.world.manager.WorldManager;
import com.faust.lhengine.saves.RoomSaveEntry;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Casual Room class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class CasualRoom extends AbstractRoom {

    // List of map casual numbers with a Golden cross
    // redundant type definition needed for html build
    public static final List<Integer> GOLDCROSS_MAPS = new ArrayList<Integer>() {{
        this.add(1);
        this.add(5);
        this.add(6);
        this.add(10);
        this.add(15);
    }};

    //List of Map casual numbers with a bush
    // redundant type definition needed for html build
    public static final List<Integer> BUSH_MAPS = new ArrayList<Integer>() {{
        this.add(3);
        this.add(8);
        this.add(16);
    }};

    // Total number of supported casual rooms
    public static final int CASUAL_TOTAL = 16;

    public CasualRoom(WorldManager worldManager, TextBoxManager textManager, SplashManager splashManager, PlayerInstance player, OrthographicCamera camera, AssetManager assetManager, RoomSaveEntry roomSaveEntry, MusicManager musicManager) {
        super(RoomTypeEnum.CASUAL, worldManager, textManager, splashManager, player, camera, assetManager, roomSaveEntry,  musicManager);
    }

    @Override
    protected void loadTiledMap(RoomSaveEntry roomSaveEntry) {

        // If has a predefined casual number (like from a savefile or because it was already visited) use that one
        // Or else generate a new number.
        int casualNumber;
        if (roomSaveEntry.casualNumber > 0) {
            casualNumber = roomSaveEntry.casualNumber;

        } else {
            if (roomContent.roomFlags.get(RoomFlagEnum.GUARANTEED_GOLDCROSS)) {
                //pick only ones with skeleton poi
                casualNumber = GOLDCROSS_MAPS.get(MathUtils.random(0, GOLDCROSS_MAPS.size()-1));
            } else if (roomContent.roomFlags.get(RoomFlagEnum.WITHOUT_HERBS)) {
                //pick only ones without herbs in
                do {
                    casualNumber = MathUtils.random(1, CasualRoom.CASUAL_TOTAL);
                }while(BUSH_MAPS.contains(casualNumber));
            } else if (roomContent.roomFlags.get(RoomFlagEnum.GUARANTEED_HERBS)) {
                //pick only ones with herbs in
                casualNumber = BUSH_MAPS.get(MathUtils.random(0, BUSH_MAPS.size()-1));
            } else {
                casualNumber = MathUtils.random(1, CasualRoom.CASUAL_TOTAL);
            }
        }

        Gdx.app.log(LoggerUtils.DEBUG_TAG, "casualNumber: " + casualNumber);

        //Enforce number between 1 and CASUAL_TOTAL. Seemingly unnecessary, but...
        casualNumber = MathUtils.clamp(casualNumber, 1, CasualRoom.CASUAL_TOTAL);

        // Casual maps range from casual1.tmx to casual7.tmx, with a %d to be mapped
        roomContent.roomFileName = roomContent.roomFileName.replace("%d", Integer.toString(casualNumber));
        roomSaveEntry.casualNumber = casualNumber;

        // Load Tiled map
        this.roomContent.tiledMap = new TmxMapLoader().load(roomContent.roomFileName);
        //onMapChange(); tODO
    }

    @Override
    protected void onRoomEnter(RoomTypeEnum roomType, WorldManager worldManager, AssetManager assetManager, RoomSaveEntry roomSaveEntry, MapObjects mapObjects) {

        if(Objects.nonNull(roomSaveEntry)){
            roomSaveEntry.poiStates.forEach((id,isExamined)->{
                //update POI status
                POIInstance poi = this.roomContent.poiList.stream().filter(p -> id.equals(p.getPoiIdInMap())).findFirst().orElse(null);

                if(Objects.nonNull(poi)){
                    poi.setAlreadyExamined(isExamined);
                }
            });
        }
        if (!roomContent.enemyList.isEmpty()) {
            //Loop title music
            musicManager.playMusic(TuneEnum.DANGER, 0.75f);
        } else {
            //Loop title music
            musicManager.playMusic(TuneEnum.AMBIENCE, 0.85f);
        }
    }

    @Override
    public void onRoomLeave(RoomSaveEntry roomSaveEntry) {
        roomContent.poiList.forEach(poiInstance -> roomSaveEntry.poiStates.put(poiInstance.getPoiIdInMap(),poiInstance.isAlreadyExamined()));
        roomContent.removedPoiList.forEach(poiInstance -> roomSaveEntry.poiStates.put(poiInstance.getPoiIdInMap(), poiInstance.isAlreadyExamined()));

        //always enable enemies
        roomContent.roomFlags.put(RoomFlagEnum.DISABLED_ENEMIES, false);
    }

    @Override
    public String getLayerToDraw() {
        return MapLayersEnum.TERRAIN_LAYER.getLayerName();
    }
}
