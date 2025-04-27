package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.Arrays;

/**
 * Tutorial Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class TutorialEntity extends AnimatedEntity {

    public TutorialEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/tutorial_sheet.png"));
    }

    @Override
    protected void initAnimations() {

        TextureRegion[] allFrames = getFramesFromTexture();

        TextureRegion[] arrowsFrames = Arrays.copyOfRange(allFrames, 0, getTextureColumns());
        TextureRegion[] wasdFrames = Arrays.copyOfRange(allFrames, getTextureColumns(), getTextureColumns() * 2);
        TextureRegion[] zFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 2, getTextureColumns() * 3);
        TextureRegion[] xFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 3, getTextureColumns() * 4);
        TextureRegion[] cFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 4, getTextureColumns() * 5);
        TextureRegion[] kFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 5, getTextureColumns() * 6);
        TextureRegion[] jFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 6, getTextureColumns() * 7);
        TextureRegion[] lFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 7, getTextureColumns() * 8);

        // Initialize the Dead frame
        addAnimationForDirection(new Animation<>(FRAME_DURATION, arrowsFrames), GameBehavior.WALK, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, wasdFrames), GameBehavior.WALK, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, zFrames), GameBehavior.ATTACK, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, jFrames), GameBehavior.ATTACK, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, xFrames), GameBehavior.IDLE, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, kFrames), GameBehavior.IDLE, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, cFrames), GameBehavior.KNEE, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, lFrames), GameBehavior.KNEE, DirectionEnum.DOWN);

    }

    @Override
    protected int getTextureColumns() {
        return 4;
    }

    @Override
    protected int getTextureRows() { return 8; }

}
