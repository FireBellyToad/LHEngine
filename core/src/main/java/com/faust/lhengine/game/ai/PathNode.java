package com.faust.lhengine.game.ai;

import com.badlogic.gdx.math.Vector2;

/**
 * Path node model
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class PathNode extends Vector2 {

    public int index = 0;

    public PathNode(Vector2 position) {
        super(position);
    }

    public PathNode(float x, float y) {
        super(x,y);
    }

    @Override
    public boolean equals(Object obj) {
        return  super.equals(obj) && ((PathNode) obj).index == this.index;
    }
}
