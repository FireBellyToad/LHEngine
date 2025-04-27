package com.faust.lhengine.game.world.interfaces;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

/**
 * Raycaster interface, used by raycaster (Box2D world)
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public interface RayCaster {

    void rayCast(RayCastCallback callback, Vector2 from, Vector2 to);
}
