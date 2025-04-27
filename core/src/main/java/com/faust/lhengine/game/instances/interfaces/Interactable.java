package com.faust.lhengine.game.instances.interfaces;

import com.faust.lhengine.game.instances.impl.PlayerInstance;

/**
 * Interface for describing interaction with player instance
 */
public interface Interactable {

    void doPlayerInteraction(PlayerInstance playerInstance);
    
    void endPlayerInteraction(PlayerInstance playerInstance);
}
