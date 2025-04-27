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
 * Bounded enemy Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class FyingCorpseEntity extends AnimatedEntity {

    private final Sound hurtCry;
    private final Sound deathCry;
    private final Sound evadeSwift;
    private final Texture shadow;

    public FyingCorpseEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/bounded_sheet.png"));
        shadow = assetManager.get("sprites/shadow.png");
        hurtCry = assetManager.get("sounds/SFX_shot4.ogg");
        deathCry = assetManager.get("sounds/SFX_creatureDie4.ogg");
        evadeSwift = assetManager.get("sounds/evade.ogg");
    }

    @Override
    protected void initAnimations() {

        TextureRegion[] allFrames = getFramesFromTexture();

        TextureRegion[] idleFramesDown = Arrays.copyOfRange(allFrames, 0, getTextureColumns());
        TextureRegion[] idleFramesLeft = Arrays.copyOfRange(allFrames, getTextureColumns(), getTextureColumns() * 2);
        TextureRegion[] idleFramesUp = Arrays.copyOfRange(allFrames, getTextureColumns() * 2, getTextureColumns() * 3);
        TextureRegion[] idleFramesRight = Arrays.copyOfRange(allFrames, getTextureColumns() * 3, getTextureColumns() * 4);
        TextureRegion[] walkFramesDown = Arrays.copyOfRange(allFrames, getTextureColumns() * 4, getTextureColumns() * 5);
        TextureRegion[] walkFramesLeft = Arrays.copyOfRange(allFrames, getTextureColumns() * 5, getTextureColumns() * 6);
        TextureRegion[] walkFramesUp = Arrays.copyOfRange(allFrames, getTextureColumns() * 6, getTextureColumns() * 7);
        TextureRegion[] walkFramesRight = Arrays.copyOfRange(allFrames, getTextureColumns() * 7, getTextureColumns() * 8);
        TextureRegion[] attackFramesDown = Arrays.copyOfRange(allFrames, getTextureColumns() * 8, getTextureColumns() * 9);
        TextureRegion[] attackFramesLeft = Arrays.copyOfRange(allFrames, getTextureColumns() * 9, getTextureColumns() * 10);
        TextureRegion[] attackFramesUp = Arrays.copyOfRange(allFrames, getTextureColumns() * 10, getTextureColumns() * 11);
        TextureRegion[] attackFramesRight = Arrays.copyOfRange(allFrames, getTextureColumns() * 11, getTextureColumns() * 12);
        TextureRegion[] deadFrame = Arrays.copyOfRange(allFrames, getTextureColumns() * 12, 1+(getTextureColumns() * 12));

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

        // Initialize the Walk Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, attackFramesDown), GameBehavior.ATTACK, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, attackFramesLeft), GameBehavior.ATTACK, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, attackFramesUp), GameBehavior.ATTACK, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, attackFramesRight), GameBehavior.ATTACK, DirectionEnum.RIGHT);

        // Initialize the Hurt Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesDown), GameBehavior.HURT, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesLeft), GameBehavior.HURT, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesUp), GameBehavior.HURT, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesRight), GameBehavior.HURT, DirectionEnum.RIGHT);

        // Initialize the Evade Animation with the frame interval and array of frames
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesDown), GameBehavior.EVADE, DirectionEnum.DOWN);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesLeft), GameBehavior.EVADE, DirectionEnum.LEFT);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesUp), GameBehavior.EVADE, DirectionEnum.UP);
        addAnimationForDirection(new Animation<>(FRAME_DURATION, walkFramesRight), GameBehavior.EVADE, DirectionEnum.RIGHT);

        // Initialize the Dead frame
        addAnimation(new Animation<>(FRAME_DURATION, deadFrame), GameBehavior.DEAD);

    }

    @Override
    protected int getTextureColumns() {
        return 6;
    }

    @Override
    protected int getTextureRows() { return 13; }

    public Texture getShadowTexture() {
        return shadow;
    }

    public void playHurtCry() {
        hurtCry.play();
    }

    public void playDeathCry() {
        deathCry.play();
    }

    public void playEvadeSwift() {
        evadeSwift.play();
    }


}
