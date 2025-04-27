package com.faust.lhengine.game.instances.interfaces;

import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.instances.GameInstance;

/**
 * On Behavior Change Listener
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public interface OnBehaviorChangeListener {

    /**
     *
     * @param source GameInstance which behaviour has changed
     * @param newBehaviour the new behaviour
     */
    void onBehaviourChange(GameInstance source, GameBehavior newBehaviour);
}
