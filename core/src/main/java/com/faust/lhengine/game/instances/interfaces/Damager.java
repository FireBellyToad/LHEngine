package com.faust.lhengine.game.instances.interfaces;

/**
 * Interface for describing attacking behaviour
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public interface Damager {
    /**
     * @return a random damage roll
     */
    double damageRoll();

}
