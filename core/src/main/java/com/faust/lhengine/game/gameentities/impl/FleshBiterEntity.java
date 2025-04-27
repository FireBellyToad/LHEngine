package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.Arrays;

/**
 *
 * Meat enemy Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class FleshBiterEntity extends AnimatedEntity {

    public FleshBiterEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/meat_sheet.png"));
    }

    @Override
    protected void initAnimations() {

        TextureRegion[]  allFrames = getFramesFromTexture();

        TextureRegion[] walkFrames = Arrays.copyOfRange(allFrames, 0, getTextureColumns());
        TextureRegion[] attackFrames = Arrays.copyOfRange(allFrames, getTextureColumns() , getTextureColumns() *2);

        // Initialize the Idle Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, walkFrames), GameBehavior.IDLE);
        addAnimation(new Animation<>(FRAME_DURATION, walkFrames), GameBehavior.WALK);
        addAnimation(new Animation<>(FRAME_DURATION, attackFrames), GameBehavior.ATTACK);

    }

    @Override
    protected int getTextureColumns() {
        return 4;
    }

    @Override
    protected int getTextureRows() {
        return 2;
    }

}
