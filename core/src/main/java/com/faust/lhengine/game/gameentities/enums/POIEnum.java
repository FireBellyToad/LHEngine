package com.faust.lhengine.game.gameentities.enums;

/**
 * POI Types enum
 *
 * @author Jacopo "Faust" Buttiglieri
 *
 */
public enum POIEnum {
    SKELETON("poi.skull", ItemEnum.GOLDCROSS, "splash.goldcross"),
    BUSH("poi.bush", ItemEnum.HEALTH_KIT),
    SOIL("poi.soil", ItemEnum.HOLY_LANCE, "splash.holy"),
    CADAVER("poi.cadaver", ItemEnum.ARMOR),
    BROTHER("poi.brother"),
    ECHO_CORPSE("poi.echocorpse"),
    BURNT_PAPER("poi.burntpaper"),
    BAPTISMAL("poi.baptismal", ItemEnum.HOLY_WATER, ItemEnum.WATERSKIN),
    BURNT_MONK("poi.burntmonk", "splash.burntmonk"),
    ALTAR("poi.altar", null,"splash.diaconus", ItemEnum.HOLY_WATER, false),
    WATERSKIN("poi.waterskin", ItemEnum.WATERSKIN, "splash.waterskin", ItemEnum.RITUAL, true),
    PARCHMENT("poi.parchment"),
    RITUAL("poi.ritual",ItemEnum.RITUAL,"splash.ritual", null,true),
    PLEA("poi.plea"),
    MICHAEL("poi.michael", null,"splash.michael", null, false);

    private final String textKey;
    private final ItemEnum itemGiven;
    private final String splashKey;
    private final ItemEnum itemRequired;
    private final Boolean isRemovableOnExamination;

    POIEnum(String textKey) {
        this(textKey, null, "", null, false);
    }

    POIEnum(String textKey, ItemEnum itemGiven, String splashKey) {
        this(textKey, itemGiven, splashKey, null, false);
    }

    POIEnum(String textKey, ItemEnum itemGiven, ItemEnum itemRequired) {
        this(textKey, itemGiven, "", itemRequired, false);
    }

    POIEnum(String textKey, ItemEnum itemGiven, String splashKey, ItemEnum itemRequired, boolean isRemovableOnExamination) {
        this.textKey = textKey;
        this.itemGiven = itemGiven;
        this.splashKey = splashKey;
        this.itemRequired = itemRequired;
        this.isRemovableOnExamination = isRemovableOnExamination;
    }

    POIEnum(String textKey, ItemEnum itemGiven) {
        this(textKey, itemGiven, "", null, false);
    }

    POIEnum(String textKey, String splashKey) {
        this(textKey, null, splashKey, null, false);
    }


    public String getTextKey() {

        return textKey;
    }

    public ItemEnum getItemGiven() {
        return itemGiven;
    }

    public String getSplashKey() {
        return splashKey;
    }

    public ItemEnum getItemRequired() {
        return itemRequired;
    }

    public Boolean getRemovableOnExamination() {
        return isRemovableOnExamination;
    }
}
