package com.faust.lhengine.cutscenes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.TexturedEntity;
import com.faust.lhengine.game.gameentities.SpriteEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.impl.PlayerEntity;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.utils.ShaderWrapper;

import java.util.*;

/**
 * Actor for using GameEntities outside of Game scope with animation handling. Used usually in cutscenes
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class SimpleActor {

    private final TexturedEntity entity;
    private final Vector2 position;
    private final GameBehavior currentBehavior;
    private final DirectionEnum direction;
    private final Set<SimpleActorParametersEnum> params;
    private long startToFlickTime;
    private boolean mustFlicker = false;

    /**
     * Costructor
     *
     *  @param entity
     * @param currentBehavior
     * @param direction
     * @param x
     * @param y
     * @param params
     */
    public SimpleActor(TexturedEntity entity, GameBehavior currentBehavior, DirectionEnum direction, float x, float y, Set<SimpleActorParametersEnum> params) {
        this.entity = entity;
        this.currentBehavior = currentBehavior;
        this.direction = direction;
        this.position = new Vector2(x, y);
        this.params = params;
        this.startToFlickTime = TimeUtils.nanoTime();
    }

    /**
     * Draws the actor
     *
     * @param batch
     * @param stateTime
     */
    @SuppressWarnings("ConstantConditions")
    public void draw(SpriteBatch batch, float stateTime) {
        Objects.requireNonNull(batch);
        TextureRegion frame = null;

        ShaderWrapper shader = null;
        if(params.contains(SimpleActorParametersEnum.IS_SHADED)){
            shader = setShaderOn(batch);
        }

        if (entity instanceof PlayerEntity) {
            batch.begin();
            batch.draw(((PlayerEntity) entity).getShadowTexture(), MathUtils.floor(position.x) - 16, MathUtils.floor(position.y)-8);
            batch.end();
        }

        if (entity instanceof AnimatedEntity) {
            frame = ((AnimatedEntity) entity).getFrame(currentBehavior, direction, stateTime, true);
        } else if (entity instanceof SpriteEntity) {
            frame = ((SpriteEntity) entity).getFrame(0);
        }

        Objects.requireNonNull(frame);

        if(canBeDrawn()){
            batch.begin();
            batch.draw(frame, MathUtils.floor(position.x) - 16, MathUtils.floor(position.y)-8);
            batch.end();
        }


        if(params.contains(SimpleActorParametersEnum.IS_SHADED)){
            //Restore default shader
            shader.resetDefaultShader(batch);
        }
    }

    private boolean canBeDrawn() {

        //Flicker if has MUST_FLICKER param
        final boolean mustFlickerCheck = params.contains(SimpleActorParametersEnum.MUST_FLICKER);

        // Every 1/8 seconds alternate between showing and hiding the texture to achieve flickering effect
        if (mustFlickerCheck && TimeUtils.timeSinceNanos(startToFlickTime) > GameScreen.FLICKER_DURATION_IN_NANO) {
            mustFlicker = !mustFlicker;

            // restart flickering timer
            startToFlickTime = TimeUtils.nanoTime();
        }

        return !mustFlickerCheck || !mustFlicker;
    }

    /**
     * Activate shader
     * @return the activated shader
     * @param batch
     */
    private ShaderWrapper setShaderOn(SpriteBatch batch) {
        ShaderWrapper shader = null;

        if(entity instanceof PlayerEntity) {
            boolean hasArmor = params.contains(SimpleActorParametersEnum.PLAYER_HAS_ARMOR);
            boolean hasLance = params.contains(SimpleActorParametersEnum.PLAYER_HAS_LANCE);
            shader = ((PlayerEntity) entity).getPlayerShader();
            shader.addFlag("hasArmor", hasArmor);
            shader.addFlag("hasHolyLance", hasLance);
            shader.setShaderOnBatchWithFlags(batch);
        }

        Objects.requireNonNull(shader,"Trying to activate a shader without giving a real entity!");

        return shader;
    }

}
