package com.faust.lhengine.saves.enums;

/**
 * Enum for save fields
 * f
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum SaveFieldsEnum {

    LANCE("lance"),
    CROSSES("crosses"),
    ARMOR("armor"),
    DAMAGE("damage"),
    HERBS_AVAILABLE("herbsAvailable"),
    HERBS_FOUND("herbsFound"),
    KILLED_SECRET("killedSecret"),
    RITUAL("ritual"),
    WATERSKIN("waterskin"),
    HOLY_WATER("holy_water"),
    PLAYER_INFO("playerInfo", new SaveFieldsEnum[]{LANCE, CROSSES, ARMOR, DAMAGE, HERBS_AVAILABLE, HERBS_FOUND, KILLED_SECRET, RITUAL, HOLY_WATER, WATERSKIN}),

    X("x"),
    Y("y"),
    CASUAL_NUMBER("casualNumber"),
    SAVED_FLAGS("savedFlags"),
    POI_STATES("poiStates"),
    ROOMS("rooms", new SaveFieldsEnum[]{X, Y, CASUAL_NUMBER, SAVED_FLAGS, POI_STATES});

    private final String fieldName;
    private final SaveFieldsEnum[] subFields;

    SaveFieldsEnum(String fieldName) {
        this(fieldName, null);
    }

    SaveFieldsEnum(String fieldName, SaveFieldsEnum[] subFields) {
        this.fieldName = fieldName;
        this.subFields = subFields;
    }

    public String getFieldName() {
        return fieldName;
    }

    public SaveFieldsEnum[] getSubFields() {
        return subFields;
    }
}
