package com.faust.lhengine.saves;

import com.faust.lhengine.game.rooms.enums.RoomFlagEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * Wrapper Class for saving room
 */
public class RoomSaveEntry implements Serializable {

    public int casualNumber;
    public final int x;
    public final int y;
    public final Map<RoomFlagEnum,Boolean> savedFlags;
    public final Map<Integer,Boolean> poiStates;

    public RoomSaveEntry(int x, int y, int casualNumber, Map<RoomFlagEnum,Boolean> savedFlags, Map<Integer,Boolean> examinedPois) {
        this.casualNumber = casualNumber;
        this.x = x;
        this.y = y;
        this.savedFlags = savedFlags;
        this.poiStates = examinedPois;
    }

}
