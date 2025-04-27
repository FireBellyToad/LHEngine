package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.Arrays;

/**
 *
 * Strix enemy Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MonsterBirdEntity extends AnimatedEntity {

    private final Sound hurtCry;
    private final Sound deathCry;
    private final Sound leechSound;
    private final Texture shadow;

    public MonsterBirdEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/strix_sheet.png"));
        shadow = assetManager.get("sprites/shadow.png");
        hurtCry = assetManager.get("sounds/SFX_hit&damage2.ogg");
        deathCry = assetManager.get("sounds/SFX_creatureDie4.ogg");
        leechSound = assetManager.get("sounds/SFX_hit&damage6.ogg");
    }

    @Override
    protected void initAnimations() {

        TextureRegion[] allFrames = getFramesFromTexture();

        TextureRegion[] idleFramesDown = Arrays.copyOfRange(allFrames, 0, getTextureColumns());
        TextureRegion[] idleFramesLeft = Arrays.copyOfRange(allFrames, getTextureColumns(), getTextureColumns()*2);
        TextureRegion[] idleFramesUp = Arrays.copyOfRange(allFrames, getTextureColumns()*2, getTextureColumns() * 3);
        TextureRegion[] idleFramesRight = Arrays.copyOfRange(allFrames, getTextureColumns()*3, getTextureColumns() * 4);
        TextureRegion[] walkFramesDown = Arrays.copyOfRange(allFrames, getTextureColumns() * 4, getTextureColumns() * 5);
        TextureRegion[] walkFramesLeft = Arrays.copyOfRange(allFrames, getTextureColumns() * 5, getTextureColumns() * 6);
        TextureRegion[] walkFramesUp = Arrays.copyOfRange(allFrames, getTextureColumns() * 6, getTextureColumns() * 7);
        TextureRegion[] walkFramesRight = Arrays.copyOfRange(allFrames, getTextureColumns() * 7, getTextureColumns() * 8);
        TextureRegion[] attachedFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 8, getTextureColumns() * 9);
        TextureRegion[] deadFrame = Arrays.copyOfRange(allFrames, getTextureColumns() * 9, (getTextureColumns() * 9)+1);

        // Initialize the Idle Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, idleFramesDown), GameBehavior.IDLE, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, idleFramesLeft), GameBehavior.IDLE, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, idleFramesUp), GameBehavior.IDLE, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, idleFramesRight), GameBehavior.IDLE, DirectionEnum.RIGHT);

        // Initialize the Walk Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesDown), GameBehavior.WALK, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesLeft), GameBehavior.WALK, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesUp), GameBehavior.WALK, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesRight), GameBehavior.WALK, DirectionEnum.RIGHT);

        addAnimation(new Animation<>(FRAME_DURATION, attachedFrames), GameBehavior.ATTACK);

        // Initialize the Hurt Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesDown), GameBehavior.HURT, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesLeft), GameBehavior.HURT, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesUp), GameBehavior.HURT, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesRight), GameBehavior.HURT, DirectionEnum.RIGHT);

        // Initialize the Dead frame
        addAnimation(new Animation<>(FRAME_DURATION, deadFrame), GameBehavior.DEAD);

    }

    @Override
    protected int getTextureColumns() {
        return 4;
    }

    @Override
    protected int getTextureRows() {
        return 10;
    }

    public Texture getShadowTexture() {
        return shadow;
    }

    public void playHurtCry() {
        hurtCry.play();
    }

    public void playDeathCry() {
        deathCry.play();
    }

    public void playLeechSound() {
        leechSound.loop(0.5f);
    }
    public void stopLeechSound() {
        leechSound.pause();
    }
}
