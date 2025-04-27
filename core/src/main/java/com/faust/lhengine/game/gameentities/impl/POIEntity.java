package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.faust.lhengine.game.gameentities.SpriteEntity;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.gameentities.enums.POIEnum;

/**
 * POI Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class POIEntity extends SpriteEntity {

    public static final String FOUND_ITEM_MESSAGE_KEY_SUFFIX = ".success";
    public static final String EXAMINING_ITEM_MESSAGE_KEY_SUFFIX = ".examine";

    private final String messageKey;
    private final ItemEnum itemGiven;
    private final String splashKey;
    private final POIEnum type;
    private final ItemEnum itemRequired;
    private final Boolean isRemovableOnExamination;

    public POIEntity(POIEnum type, AssetManager assetManager) {
        super(assetManager.get("sprites/poi_sheet.png"), type.ordinal());
        this.messageKey = type.getTextKey();
        this.itemGiven = type.getItemGiven();
        this.splashKey = type.getSplashKey();
        this.itemRequired = type.getItemRequired();
        this.isRemovableOnExamination = type.getRemovableOnExamination();
        this.type = type;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public ItemEnum getItemGiven() {
        return itemGiven;
    }

    public ItemEnum getItemRequired() {
        return itemRequired;
    }

    public String getSplashKey() {
        return splashKey;
    }

    public Boolean getRemovableOnExamination() {
        return isRemovableOnExamination;
    }

    @Override
    protected int getTextureColumns() {
        return 1;
    }

    @Override
    protected int getTextureRows() {
        return POIEnum.values().length;
    }

    public POIEnum getType() {
        return type;
    }
}
