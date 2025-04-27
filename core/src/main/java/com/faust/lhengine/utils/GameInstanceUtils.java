package com.faust.lhengine.utils;

import com.faust.lhengine.game.instances.GameInstance;

import java.util.List;
import java.util.Objects;

/**
 * Generic Game Instances Utils
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class GameInstanceUtils {


    /**
     * Returns the nearest GameInstance from a GameInstance, taken in a list.
     *
     * @param nearestFrom the GameInstance to check from
     * @param instanceList the list which contains the GameInstance to return
     *
     * @return the nearest Instance from this in the room
     */
    public static  <T extends GameInstance,K extends GameInstance> K getNearestInstance(T nearestFrom,List<K> instanceList) {
        Objects.requireNonNull(instanceList);

        K nearest = null;

        for (K inst : instanceList) {
            // In no nearest, just return the first one
            if (Objects.isNull(nearest)) {
                nearest = inst;
            } else if (nearest.getBody().getPosition().dst(nearestFrom.getBody().getPosition()) > inst.getBody().getPosition().dst(nearestFrom.getBody().getPosition())) {
                nearest = inst;
            }
        }
        return nearest;

    }
}
