package com.faust.lhengine.utils;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Vector2;
import com.faust.lhengine.game.ai.PathNode;

/**
 * Util class for finding path
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class PathfinderUtils {

    //This estimator just evaluates distance between two nodes
    private static final Heuristic<PathNode> ESTIMATOR = Vector2::dst;

    /**
     * Generate an A* paths between start node and end node both in a node graph
     * @param startNode
     * @param goalNode
     * @param indexedGraph
     * @return
     */
    public static GraphPath<PathNode> generatePath(PathNode startNode, PathNode goalNode, IndexedGraph<PathNode> indexedGraph) {
        final GraphPath<PathNode> nodePath = new DefaultGraphPath<>();
        new IndexedAStarPathFinder<>(indexedGraph).searchNodePath(startNode, goalNode, ESTIMATOR, nodePath);
        return nodePath;
    }
}
