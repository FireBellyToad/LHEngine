package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.faust.lhengine.game.gameentities.SpriteEntity;
import com.faust.lhengine.game.gameentities.enums.DecorationsEnum;

/**
 * Entity class for Decorations
 */
public class DecorationEntity extends SpriteEntity {

    private final DecorationsEnum type;
    private final Sound grassMove;

    public DecorationEntity(DecorationsEnum decorationType, AssetManager assetManager) {
        super(assetManager.get("sprites/decorations_sheet.png"), decorationType.ordinal());

        this.type = decorationType;
        this.grassMove = assetManager.get("sounds/SFX_shot5.ogg");
    }

    @Override
    protected int getTextureColumns() {
        return 2;
    }

    @Override
    protected int getTextureRows() {
        return DecorationsEnum.values().length;
    }

    public DecorationsEnum getType() {
        return type;
    }

    /**
     *
     * @return true if decoration is passable
     */
    public boolean isPassable() {
        switch (type){
            case GRASS:
            case TUMOR:
            case PLANT:
            case PAPER:
            case DEAD_TREE:{
                return true;
            }
            default:{
                return false;
            }
        }
    }

    public void playGrassMove(){
        this.grassMove.play();
    }
}
