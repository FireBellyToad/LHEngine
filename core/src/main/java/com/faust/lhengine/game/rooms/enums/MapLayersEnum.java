package com.faust.lhengine.game.rooms.enums;

/**
 * Map Layer enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum MapLayersEnum {
    TERRAIN_LAYER("terrain"),
    OVERLAY_LAYER("overlay"),
    OBJECT_LAYER("objects"),
    ECHO_LAYER("echo");

    private final String layerName;

    MapLayersEnum(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }
}