package com.faust.lhengine.saves.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.saves.AbstractSaveFileManager;
import com.faust.lhengine.saves.RoomSaveEntry;
import com.faust.lhengine.saves.enums.SaveFieldsEnum;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.*;

/**
 * Desktop save File Manager class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class DesktopSaveFileManager extends AbstractSaveFileManager {

    private final String selectedFileName = "saves/mainWorldSave.json";

    /**
     * Load game and populate game instances with the loaded values
     *
     * @param player
     * @param saveMap
     * @throws SerializationException
     */
    public void loadSaveForGame(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap) throws SerializationException {

        FileHandle file = Gdx.files.local(selectedFileName);

        if (!file.exists()) {
            return;
        }

        JsonValue fileContent = new JsonReader().parse(file);

        if (Objects.isNull(fileContent)) {
            return;
        }
        //Player Info
        setPlayerInfo(player, fileContent);

        //Room Info
        setRoomInfo(saveMap, fileContent);
    }

    /**
     * Save on filesystem the predefined numbers of the casual rooms
     */
    public void saveOnFile(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap) {

        String stringSave = getStringSaveFile(player, saveMap);
        Gdx.files.local(selectedFileName).writeString(stringSave, false);
    }

    /**
     * Clean saveFile for new game
     */
    public void cleanSaveFile() {

        Gdx.files.local(selectedFileName).writeString("", false);
    }

    /**
     * Load game and put raw loaded values in map
     *
     * @return
     */
    public Map<String, Object> loadRawValues() {

        FileHandle file = Gdx.files.local(selectedFileName);

        if (!file.exists()) {
            return null;
        }

        JsonValue fileRead = null;
        try {
            fileRead = new JsonReader().parse(file);
        } catch (Exception e) {
            Gdx.app.log(LoggerUtils.DEBUG_TAG, e.getMessage());
        }

        if (Objects.isNull(fileRead)) {
            return null;
        }

        JsonValue playerInfo = fileRead.get(SaveFieldsEnum.PLAYER_INFO.getFieldName());
        if (Objects.isNull(playerInfo)) {
            return null;
        }

        Map<String, Object> rawValuesMap = new HashMap<>();

        //All subfields excluding Armor (which is not int)
        for (SaveFieldsEnum subField : SaveFieldsEnum.PLAYER_INFO.getSubFields()) {
            if (!SaveFieldsEnum.ARMOR.equals(subField) && !SaveFieldsEnum.KILLED_SECRET.equals(subField) ) {
                rawValuesMap.put(subField.getFieldName(), playerInfo.getInt(subField.getFieldName()));
            }
        }

        rawValuesMap.put(SaveFieldsEnum.ARMOR.getFieldName(), playerInfo.getBoolean(SaveFieldsEnum.ARMOR.getFieldName()));
        rawValuesMap.put(SaveFieldsEnum.KILLED_SECRET.getFieldName(), playerInfo.getBoolean(SaveFieldsEnum.ARMOR.getFieldName()));

        return rawValuesMap;
    }
}
