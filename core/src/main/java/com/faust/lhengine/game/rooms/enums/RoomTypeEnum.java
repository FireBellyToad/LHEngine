package com.faust.lhengine.game.rooms.enums;

/**
 * Room type enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum RoomTypeEnum {

    EMPTY_SPACE(""),
    CASUAL("casual%d.tmx"),
    CEMETERY_TOP("cemeteryTop.tmx"),
    CEMETERY_CENTER("cemeteryCenter.tmx"),
    CEMETERY_RIGHT("cemeteryRight.tmx"),
    CHURCH_ENTRANCE("churchEntrance.tmx"),
    CHURCH_RIGHT("churchRight.tmx"),
    CHURCH_BOTTOM("churchBottom.tmx"),
    CHURCH_LEFT("churchLeft.tmx"),
    TREE_STUMP("treeStump.tmx"),
    BOAT("boat.tmx"),
    START_POINT("start.tmx"),
    CRUCIFIED("crucified.tmx"),
    FINAL("final.tmx"),
    BOAT_LEFT("boatLeft.tmx"),
    BOAT_DOWN("boatDown.tmx"),
    INFERNUM("infernum.tmx"),
    START_LEFT("startLeft.tmx"),
    START_RIGHT("startRight.tmx"),
    PEDESTAL("pedestal.tmx"),
    TEST("scriptTestChamber.tmx");

    private final String mapFileName;

    RoomTypeEnum(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    /**
     * @param roomTypeEnum
     * @return true if roomType has echo
     */
    public static boolean hasEchoes(RoomTypeEnum roomTypeEnum) {
        return CRUCIFIED.equals(roomTypeEnum) ||
                TREE_STUMP.equals(roomTypeEnum) ||
                CEMETERY_CENTER.equals(roomTypeEnum) ||
                BOAT.equals(roomTypeEnum) ||
                INFERNUM.equals(roomTypeEnum);
    }

    public String getMapFileName() {
        return mapFileName;
    }
}
