package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.utils.ShaderWrapper;

import java.util.Arrays;

/**
 * Player entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class PlayerEntity extends AnimatedEntity {

    private final ShaderWrapper playerShader;
    private final Texture shadow;
    private final Sound bonus;
    private final Sound hurtCry;
    private final Sound lanceSwing;
    private final Sound waterSplash;
    private final Sound deathCry;

    private final ParticleEffect waterWalkEffect;

    public PlayerEntity(AssetManager assetManager, boolean isWebBuild) {
        super(assetManager.get("sprites/walfrit_sheet.png"));
        shadow = assetManager.get("sprites/shadow.png");
        bonus = assetManager.get("sounds/SFX_collect&bonus13.ogg");
        hurtCry = assetManager.get("sounds/SFX_hit&damage13.ogg");
        lanceSwing = assetManager.get("sounds/SFX_swordSwing.ogg");
        waterSplash = assetManager.get("sounds/SFX_waterSplash.ogg");
        deathCry = assetManager.get("sounds/death_scream.ogg");
        playerShader = new ShaderWrapper("shaders/player_vertex.glsl","shaders/player_fragment.glsl",isWebBuild);

        // Init waterwalk effect
        waterWalkEffect = new ParticleEffect();
        // First is particle configuration, second is particle sprite path (file is embeeded in configuration)
        waterWalkEffect.load(Gdx.files.internal("particles/waterwalk"), Gdx.files.internal("sprites/"));
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
        TextureRegion[] kneeFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 12, getTextureColumns() * 13);
        TextureRegion[] deadFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 13, getTextureColumns() * 14);
        TextureRegion[] dyingFrames = Arrays.copyOfRange(allFrames, getTextureColumns() * 14, getTextureColumns() * 15);

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

        // Initialize the Knee Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, kneeFrames), GameBehavior.KNEE);

        // Initialize the Laying Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, deadFrames), GameBehavior.LAYING);

        // Initialize the Dead Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, dyingFrames), GameBehavior.DEAD);


    }

    @Override
    protected int getTextureColumns() {
        return 12;
    }

    @Override
    protected int getTextureRows() {
        return 15;
    }

    public Texture getShadowTexture() {
        return shadow;
    }

    public void playBonusSound() {
        bonus.play();
    }

    public void playHurtCry() {
        hurtCry.play();
    }

    public void playLanceSwing() {
        lanceSwing.play();
    }

    public void playWaterSplash() {
        waterSplash.play();
    }

    public void playDeathCry() { deathCry.play(); }

    public ShaderWrapper getPlayerShader() {
        return playerShader;
    }

    public ParticleEffect getWaterWalkEffect() {
        return waterWalkEffect;
    }
}
