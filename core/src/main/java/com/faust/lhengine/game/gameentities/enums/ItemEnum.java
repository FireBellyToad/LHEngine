package com.faust.lhengine.game.gameentities.enums;

/**
 * Items enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum ItemEnum {
    HEALTH_KIT,
    HOLY_LANCE(true),
    GOLDCROSS,
    ARMOR,
    RITUAL,
    WATERSKIN,
    HOLY_WATER;

    final boolean hasMultipleSplashes;

    ItemEnum() {
        hasMultipleSplashes = false;
    }
    ItemEnum(boolean hasMultipleSplashes) {
        this.hasMultipleSplashes = hasMultipleSplashes;
    }

    public boolean hasMultipleSplashes() {
        return hasMultipleSplashes;
    }
}
