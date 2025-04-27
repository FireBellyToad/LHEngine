package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.faust.lhengine.game.gameentities.SpriteEntity;

/**
 * Half Dakrness Entity class, to be used only in cutscenes
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class HalfDarknessEntity extends SpriteEntity {

    public HalfDarknessEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/darkness_overlay.png"), 1);
    }

    @Override
    protected int getTextureColumns() {
        return 1;
    }

    @Override
    protected int getTextureRows() {
        return 2;
    }

}
