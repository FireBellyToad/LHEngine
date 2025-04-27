package com.faust.lhengine.game.instances;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.faust.lhengine.game.gameentities.TexturedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.instances.interfaces.OnBehaviorChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Animated entity instanced in game world class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public abstract class AnimatedInstance extends GameInstance {

    protected int damage = 0;

    //is private because it must be never change with assignment outside of this scope!
    private GameBehavior currentBehavior = GameBehavior.IDLE;

    protected DirectionEnum currentDirectionEnum = DirectionEnum.UNUSED;
    protected final List<OnBehaviorChangeListener> onBehaviorChangeListenerList = new ArrayList<>();
    protected Body hitBox;

    protected AnimatedInstance(final TexturedEntity entity) {
        super(entity);
    }

    /**
     * Utility for extracting Direction from a directionNormal normal
     */
    protected DirectionEnum extractDirectionFromNormal(Vector2 directionNormal) {

        if (directionNormal.x <= -0.5) {
            return DirectionEnum.LEFT;
        } else if (directionNormal.x > 0.5) {
            return DirectionEnum.RIGHT;
        }

        if (directionNormal.y < 0) {
            return DirectionEnum.DOWN;
        } else {
            return DirectionEnum.UP;
        }
    }

    @Override
    public void dispose() {
        if(!Objects.isNull(hitBox)){
            this.hitBox.getFixtureList().forEach(f ->
                    hitBox.destroyFixture(f));
        }
        super.dispose();
    }

    public GameBehavior getCurrentBehavior() {
        return currentBehavior;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * change this AnimatedInstance current behavior, notifying listeners
     * @param newBehavior
     */
    public void changeCurrentBehavior(GameBehavior newBehavior){
        Objects.requireNonNull(newBehavior);

        currentBehavior = newBehavior;

        //Noitify listeners
        if(!onBehaviorChangeListenerList.isEmpty()){
            for(OnBehaviorChangeListener listener : onBehaviorChangeListenerList){
                listener.onBehaviourChange(this, newBehavior);
            }
        }

    }

    /**
     * Add a OnBehaviorChangeListener
     * @param listener
     */
    public void addOnBehaviorChangeListener(OnBehaviorChangeListener listener){
        Objects.requireNonNull(listener);
        onBehaviorChangeListenerList.add(listener);

    }
}
