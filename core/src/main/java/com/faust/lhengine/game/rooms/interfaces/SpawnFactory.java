package com.faust.lhengine.game.rooms.interfaces;

import com.faust.lhengine.game.instances.GameInstance;

/**
 * Interface for adding game instances in something during runtime (spawning)
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public interface SpawnFactory {

    <T extends GameInstance> void spawnInstance(Class<T> instanceClass, float startX, float startY, String instanceIdentifierEnum);
}
