package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.Arrays;

/**
 *
 * Hive enemy Entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class SpitterEntity extends AnimatedEntity {

    private final Sound hurtCry;
    private final Sound deathCry;
    private final Sound spitSound;

    public SpitterEntity(AssetManager assetManager) {
        super(assetManager.get("sprites/spitter_sheet.png"));
        hurtCry = assetManager.get("sounds/SFX_hit&damage2.ogg");
        deathCry = assetManager.get("sounds/death_scream.ogg");
        spitSound = assetManager.get("sounds/spit.ogg");
    }

    @Override
    protected void initAnimations() {

        TextureRegion[]  allFrames = getFramesFromTexture();

        TextureRegion[] idleFrames = Arrays.copyOfRange(allFrames, 0, getTextureColumns());
        TextureRegion[] attackFrames = Arrays.copyOfRange(allFrames, getTextureColumns() , getTextureColumns() *2);
        TextureRegion[] deadFrame = Arrays.copyOfRange(allFrames, getTextureColumns()*2 , (getTextureColumns()*2) +1);

        // Initialize the Idle Animation with the frame interval and array of frames
        addAnimation(new Animation<>(FRAME_DURATION, idleFrames), GameBehavior.IDLE);
        addAnimation(new Animation<>(FRAME_DURATION, attackFrames), GameBehavior.ATTACK);
        addAnimation(new Animation<>(FRAME_DURATION, idleFrames), GameBehavior.HURT);
        addAnimation(new Animation<>(FRAME_DURATION, deadFrame), GameBehavior.DEAD);

    }

    @Override
    protected int getTextureColumns() {
        return 8;
    }

    @Override
    protected int getTextureRows() {
        return 3;
    }

    public void playHurtCry() {
        hurtCry.play();
    }

    public void playDeathCry() {
        deathCry.play();
    }

    public void playSpitSound() {
        spitSound.play();
    }
}
