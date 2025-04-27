package com.faust.lhengine.game.ai;

import com.badlogic.gdx.ai.pfa.Connection;

/**
 * Path model that connects two Path Nodes and precalculates its cost
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class Path implements Connection<PathNode> {

    final PathNode fromPathNode;
    final PathNode toPathNode;
    final float cost;

    public Path(PathNode fromPathNode, PathNode toPathNode){
        this.fromPathNode = fromPathNode;
        this.toPathNode = toPathNode;
        cost = fromPathNode.dst(toPathNode);
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public PathNode getFromNode() {
        return fromPathNode;
    }

    @Override
    public PathNode getToNode() {
        return toPathNode;
    }

    @Override
    public boolean equals(Object o) {
        Path otherPath = ((Path) o);
        return super.equals(o) && (otherPath.fromPathNode.equals(fromPathNode) &&
                otherPath.toPathNode.equals(toPathNode));
    }


}
