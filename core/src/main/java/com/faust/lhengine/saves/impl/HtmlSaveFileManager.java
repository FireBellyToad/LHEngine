package com.faust.lhengine.saves.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.saves.AbstractSaveFileManager;
import com.faust.lhengine.saves.RoomSaveEntry;
import com.faust.lhengine.saves.enums.SaveFieldsEnum;

import java.util.*;

/**
 * HTML Save File Manager class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class HtmlSaveFileManager extends AbstractSaveFileManager {

    @Override
    public void loadSaveForGame(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap) throws SerializationException {

        Preferences file = Gdx.app.getPreferences(selectedFileName);

        String content = (String) file.get().get(ROOT_KEY);

        if (Objects.isNull(content)) {
            return;
        }

        JsonValue fileContent = new JsonReader().parse(content);

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
        Gdx.app.getPreferences(selectedFileName).putString(ROOT_KEY, stringSave);
    }

    /**
     * Clean saveFile for new game
     */
    public void cleanSaveFile() {

        Gdx.app.getPreferences(selectedFileName).clear();
    }

    /**
     * Load game and put raw loaded values in map
     *
     * @return
     */
    public Map<String, Object> loadRawValues() {

        Preferences fileRead = Gdx.app.getPreferences(selectedFileName);

        if (Objects.isNull(fileRead)) {
            return null;
        }

        String content = (String) fileRead.get().get(ROOT_KEY);

        if (Objects.isNull(content)) {
            return null;
        }

        JsonValue root = new JsonReader().parse(content);

        if (Objects.isNull(root)) {
            return null;
        }
        JsonValue playerInfo = root.get(SaveFieldsEnum.PLAYER_INFO.getFieldName());
        if (Objects.isNull(playerInfo)) {
            return null;
        }

        Map<String, Object> rawValuesMap = new HashMap<>();

        //All subfields excluding Armor (which is not int)
        for (SaveFieldsEnum subField : SaveFieldsEnum.PLAYER_INFO.getSubFields()) {
            if (!SaveFieldsEnum.ARMOR.equals(subField)) {
                rawValuesMap.put(subField.getFieldName(), playerInfo.getInt(subField.getFieldName()));
            }
        }

        rawValuesMap.put(SaveFieldsEnum.ARMOR.getFieldName(), playerInfo.getBoolean(SaveFieldsEnum.ARMOR.getFieldName()));

        return rawValuesMap;
    }
}
