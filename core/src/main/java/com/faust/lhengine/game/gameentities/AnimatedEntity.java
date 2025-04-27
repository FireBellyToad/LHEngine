package com.faust.lhengine.game.gameentities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that extends GameEntity and adds animations handling
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public abstract class AnimatedEntity extends TexturedEntity {

    public static final float FRAME_DURATION = 0.1f;

    //Animations given Behavior and Direction
    protected final Map<GameBehavior, Map<DirectionEnum, Animation<TextureRegion>>> animations = new EnumMap<>(GameBehavior.class);

    protected AnimatedEntity(Texture texture) {
        super(texture);

        this.initAnimations();
    }

    /**
     * Initializes the animation
     */
    protected abstract void initAnimations();


    /**
     * Adds a new animation that doesn't use a Direction
     *
     * @param animation the animation itself
     * @param behavior  the behaviour associated to the animation
     */
    protected void addAnimation(Animation<TextureRegion> animation, GameBehavior behavior) {
        this.addAnimationForDirection(animation, behavior, DirectionEnum.UNUSED);
    }


    /**
     * Adds a new animation
     *
     * @param animation the animation itself
     * @param behavior  the behaviour associated to the animation
     * @param directionEnum the direction of the animation
     */
    protected void addAnimationForDirection(Animation<TextureRegion> animation, GameBehavior behavior, DirectionEnum directionEnum) {
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(directionEnum);

        this.animations.computeIfAbsent(behavior, key -> new EnumMap<>(DirectionEnum.class));

        this.animations.get(behavior).put(directionEnum, animation);
    }
    /**
     * Returns frame to render with non looping animation and Unused direction
     *
     * @param behavior  The behavior of the animation
     * @param stateTime state time to render
     * @return the TextureRegion of the frame
     */
    public TextureRegion getFrame(GameBehavior behavior, float stateTime) {
        return getFrame(behavior, DirectionEnum.UNUSED, stateTime, false);
    }

    /**
     * Returns frame to render animation with Unused direction
     *
     * @param behavior  The behavior of the animation
     * @param stateTime state time to render
     * @param looping   true if must loop
     * @return the TextureRegion of the frame
     */
    public TextureRegion getFrame(GameBehavior behavior, float stateTime, boolean looping) {
        return getFrame(behavior, DirectionEnum.UNUSED, stateTime, looping);
    }
    /**
     * Returns frame to render with looping animation
     *
     * @param behavior  The behavior of the animation
     * @param directionEnum the direction of the animation
     * @param stateTime state time to render
     * @return the TextureRegion of the frame
     */
    public TextureRegion getFrame(GameBehavior behavior, DirectionEnum directionEnum, float stateTime) {
        return getFrame(behavior, directionEnum, stateTime, true);
    }

    /**
     * Returns frame to render
     *
     * @param behavior  The behavior of the animation
     * @param directionEnum the direction of the animation
     * @param stateTime state time to render
     * @param looping   true if must loop
     * @return the TextureRegion of the frame
     */
    public TextureRegion getFrame(GameBehavior behavior, DirectionEnum directionEnum, float stateTime, boolean looping) {
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(directionEnum);

        // Get all animations for behaviour
        Map<DirectionEnum, Animation<TextureRegion>> behaviorAnimations = this.animations.get(behavior);

        Objects.requireNonNull(behaviorAnimations);

        TextureRegion toReturn;
        // If the animations doesn't have any direction, then return the unused one
        if (behaviorAnimations.containsKey(DirectionEnum.UNUSED)) {
            toReturn = behaviorAnimations.get(DirectionEnum.UNUSED).getKeyFrame(stateTime, looping);
        } else {
            toReturn = behaviorAnimations.get(directionEnum).getKeyFrame(stateTime, looping);
        }

        Objects.requireNonNull(toReturn);

        return toReturn;
    }

    /**
     * Returns current frame Index (with DirectionEnum.UNUSED)
     *
     * @param behavior  The behavior of the animation
     * @param stateTime state time to render
     * @return true if animation is finished
     */
    public int getFrameIndex(GameBehavior behavior,  float stateTime) {
        return getFrameIndex(behavior, DirectionEnum.UNUSED, stateTime);
    }

    /**
     * Returns current frame Index
     *
     * @param behavior  The behavior of the animation
     * @param directionEnum the direction of the animation
     * @param stateTime state time to render
     * @return true if animation is finished
     */
    public int getFrameIndex(GameBehavior behavior, DirectionEnum directionEnum, float stateTime) {
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(directionEnum);

        // Get all animations for behaviour
        Map<DirectionEnum, Animation<TextureRegion>> behaviorAnimations = this.animations.get(behavior);

        Objects.requireNonNull(behaviorAnimations);

        int toReturn;
        // If the animations doesn't have any direction, then return the unused one
        if (behaviorAnimations.containsKey(DirectionEnum.UNUSED)) {
            toReturn = behaviorAnimations.get(DirectionEnum.UNUSED).getKeyFrameIndex(stateTime);
        } else {
            toReturn = behaviorAnimations.get(directionEnum).getKeyFrameIndex(stateTime);
        }

        return toReturn;
    }

    /**
     * Returns if current Animation is finished
     *
     * @param behavior  The behavior of the animation
     * @param directionEnum the direction of the animation
     * @param stateTime state time to render
     * @return true if animation is finished
     */
    public boolean isAnimationFinished(GameBehavior behavior, DirectionEnum directionEnum, float stateTime) {
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(directionEnum);

        // Get all animations for behaviour
        Map<DirectionEnum, Animation<TextureRegion>> behaviorAnimations = this.animations.get(behavior);

        Objects.requireNonNull(behaviorAnimations);

        boolean toReturn;
        // If the animations doesn't have any direction, then return the unused one
        if (behaviorAnimations.containsKey(DirectionEnum.UNUSED)) {
            toReturn = behaviorAnimations.get(DirectionEnum.UNUSED).isAnimationFinished(stateTime);
        } else {
            toReturn = behaviorAnimations.get(directionEnum).isAnimationFinished(stateTime);
        }

        return toReturn;
    }

    /**
     * Returns if current Animation is finished, direction is UNUSED
     *
     * @param behavior  The behavior of the animation
     * @param stateTime state time to render
     * @return true if animation is finished
     */
    public boolean isAnimationFinished(GameBehavior behavior, float stateTime) {
        return isAnimationFinished(behavior, DirectionEnum.UNUSED,stateTime);
    }
}
