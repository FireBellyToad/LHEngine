package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.Arrays;

/**
 *
 * Escape Portal Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class EscapePortalEntity extends AnimatedEntity {

    public EscapePortalEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/escape_portal_sheet.png"));
    }

    @Override
    protected void initAnimations() {

        TextureRegion[]  allFrames = getFramesFromTexture();

        TextureRegion[] idleFrames = Arrays.copyOfRange(allFrames, 0, getTextureColumns());

        // Initialize the Idle Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, idleFrames), GameBehavior.IDLE);

    }

    @Override
    protected int getTextureColumns() {
        return 8;
    }

    @Override
    protected int getTextureRows() {
        return 1;
    }

}
