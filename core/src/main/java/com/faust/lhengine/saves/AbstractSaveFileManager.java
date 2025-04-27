package com.faust.lhengine.saves;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.gameentities.enums.PlayerFlag;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomFlagEnum;
import com.faust.lhengine.saves.enums.SaveFieldsEnum;

import java.util.*;

/**
 * @author Jacopo "Faust" Buttiglieri
 */
public abstract class AbstractSaveFileManager {

    protected static final String ROOT_KEY = "save";
    protected final Json jsonParser = new Json();
    protected final String selectedFileName = "saves/mainWorldSave";

    protected String getStringSaveFile(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap) {

        //Player info
        List<String> entries = new ArrayList<>();
        entries.add(getField(SaveFieldsEnum.LANCE.getFieldName(), player.getItemQuantityFound(ItemEnum.HOLY_LANCE)));
        entries.add(getField(SaveFieldsEnum.CROSSES.getFieldName(), player.getItemQuantityFound(ItemEnum.GOLDCROSS)));
        entries.add(getField(SaveFieldsEnum.ARMOR.getFieldName(), player.getItemQuantityFound(ItemEnum.ARMOR)));
        entries.add(getField(SaveFieldsEnum.DAMAGE.getFieldName(), player.getDamage()));
        entries.add(getField(SaveFieldsEnum.HERBS_FOUND.getFieldName(), player.getItemQuantityFound(ItemEnum.HEALTH_KIT)));
        entries.add(getField(SaveFieldsEnum.HERBS_AVAILABLE.getFieldName(), player.getAvailableHealthKits()));
        entries.add(getField(SaveFieldsEnum.WATERSKIN.getFieldName(), player.getItemQuantityFound(ItemEnum.WATERSKIN)));
        entries.add(getField(SaveFieldsEnum.RITUAL.getFieldName(), player.getItemQuantityFound(ItemEnum.RITUAL)));
        entries.add(getField(SaveFieldsEnum.HOLY_WATER.getFieldName(), player.getItemQuantityFound(ItemEnum.HOLY_WATER)));
        entries.add(getField(SaveFieldsEnum.KILLED_SECRET.getFieldName(), player.getPlayerFlagValue(PlayerFlag.HAS_KILLED_SECRET_BOSS)));

        String playerInfo = getField(SaveFieldsEnum.PLAYER_INFO.getFieldName(), String.join(",", entries), true);

        //Append rooms
        return "{" + playerInfo + "," + getField(SaveFieldsEnum.ROOMS.getFieldName(), jsonParser.toJson(saveMap.values())) + "}";
    }

    /**
     * Get string with
     *
     * @param fieldName
     * @param fieldValue
     * @param hasBrackets
     * @return
     */
    protected String getField(String fieldName, Object fieldValue, boolean hasBrackets) {
        return "\"" + fieldName + "\": " + (hasBrackets ? "{" + fieldValue + "}" : fieldValue);
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @return
     */
    protected String getField(String fieldName, Object fieldValue) {
        return getField(fieldName, fieldValue, false);
    }

    /**
     * Set room info in savemap
     *  @param saveMap
     * @param file*/
    protected void setRoomInfo(Map<RoomPosition, RoomSaveEntry> saveMap, JsonValue file) {

        //Already visited room Info
        JsonValue rooms = file.get(SaveFieldsEnum.ROOMS.getFieldName());
        if (Objects.isNull(rooms)) {
            return;
        }

        //Main loop
        RoomPosition roomPositionInCurrentSave;
        int casualNumberPredefined;
        JsonValue flagsJson;
        JsonValue poiStateJson;

        for (JsonValue roomSaveEntry : rooms) {
            roomPositionInCurrentSave = new RoomPosition(roomSaveEntry.getInt(SaveFieldsEnum.X.getFieldName()),
                    roomSaveEntry.getInt(SaveFieldsEnum.Y.getFieldName()));

            casualNumberPredefined = roomSaveEntry.getInt(SaveFieldsEnum.CASUAL_NUMBER.getFieldName());

            flagsJson = roomSaveEntry.get(SaveFieldsEnum.SAVED_FLAGS.getFieldName());
            poiStateJson = roomSaveEntry.get(SaveFieldsEnum.POI_STATES.getFieldName());

            saveMap.put(roomPositionInCurrentSave, new RoomSaveEntry(
                    roomPositionInCurrentSave.getX(),roomPositionInCurrentSave.getY(), casualNumberPredefined,
                    parseJsonFlags(flagsJson), parseJsonPoiState(poiStateJson)));
        }
    }

    /**
     * @param poiStateJson
     * @return
     */
    protected Map<Integer, Boolean> parseJsonPoiState(JsonValue poiStateJson) {
        Map<Integer, Boolean> map = new HashMap<>();

        if (Objects.nonNull(poiStateJson.child()) && Objects.nonNull(poiStateJson.child().next())) {

            //Extract child (skipping class name)
            JsonValue child = poiStateJson.child().next();

            do {
                map.put(Integer.valueOf(child.name()), child.asBoolean());
                child = child.next();
            } while (Objects.nonNull(child));
        }

        return map;
    }


    /**
     * Parse saved flag
     *
     * @param flagsJson to parse
     * @return parsed flags
     */
    protected Map<RoomFlagEnum, Boolean> parseJsonFlags(JsonValue flagsJson) {
        Map<RoomFlagEnum, Boolean> map = RoomFlagEnum.generateDefaultRoomFlags();
        boolean extractedValue;

        for (RoomFlagEnum realFlag : RoomFlagEnum.values()) {
            extractedValue = flagsJson.getBoolean(realFlag.name());
            map.put(realFlag, extractedValue);
        }

        return map;
    }

    /**
     * Sets player info from file
     *
     * @param player
     * @param file
     */
    protected void setPlayerInfo(PlayerInstance player, JsonValue file) {

        JsonValue playerInfo = file.get(SaveFieldsEnum.PLAYER_INFO.getFieldName());
        if (Objects.isNull(playerInfo)) {
            return;
        }
        player.setItemQuantityFound(ItemEnum.HOLY_LANCE,playerInfo.getInt(SaveFieldsEnum.LANCE.getFieldName()));
        player.setItemQuantityFound(ItemEnum.GOLDCROSS,playerInfo.getInt(SaveFieldsEnum.CROSSES.getFieldName()));
        player.setItemQuantityFound(ItemEnum.ARMOR,playerInfo.getInt(SaveFieldsEnum.ARMOR.getFieldName()));
        player.setDamage(playerInfo.getInt(SaveFieldsEnum.DAMAGE.getFieldName()));
        player.setItemQuantityFound(ItemEnum.HEALTH_KIT,playerInfo.getInt(SaveFieldsEnum.HERBS_FOUND.getFieldName()));
        player.setAvailableHealthKits(playerInfo.getInt(SaveFieldsEnum.HERBS_AVAILABLE.getFieldName()));
        player.setItemQuantityFound(ItemEnum.WATERSKIN,playerInfo.getInt(SaveFieldsEnum.WATERSKIN.getFieldName()));
        player.setItemQuantityFound(ItemEnum.HOLY_WATER,playerInfo.getInt(SaveFieldsEnum.HOLY_WATER.getFieldName()));
        player.setItemQuantityFound(ItemEnum.RITUAL,playerInfo.getInt(SaveFieldsEnum.RITUAL.getFieldName()));
        player.setPlayerFlagValue(PlayerFlag.HAS_KILLED_SECRET_BOSS,playerInfo.getBoolean(SaveFieldsEnum.KILLED_SECRET.getFieldName()));

    }

    /**
     * Load game and populate game instances with the loaded values
     *
     * @param player
     * @param saveMap
     * @throws SerializationException
     */
    public abstract void loadSaveForGame(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap) throws SerializationException;

    /**
     * Save on filesystem the predefined numbers of the casual rooms
     */
    public abstract void saveOnFile(PlayerInstance player, Map<RoomPosition, RoomSaveEntry> saveMap);

    /**
     * Clean saveFile for new game
     */
    public abstract void cleanSaveFile();

    /**
     * Load game and put raw loaded values in map
     *
     * @return
     */
    public abstract Map<String, Object> loadRawValues();
}
