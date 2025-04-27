package com.faust.lhengine.game.instances;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.faust.lhengine.game.ai.PathNode;
import com.faust.lhengine.game.ai.RoomNodesGraph;
import com.faust.lhengine.game.gameentities.TexturedEntity;
import com.faust.lhengine.utils.PathfinderUtils;

import java.util.Objects;

/**
 * Smart distancer instance class, used on things that should keep distance from a target smartly:
 * - if too close then do A* in nodegraph targeting the farthest node
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public abstract class DistancerInstance extends AnimatedInstance {

    private static final float DISTANCE_LIMIT_TO_EVALUATE = 70;
    //Current path node to follow
    protected PathNode targetPathNode;
    //Target instance
    protected final GameInstance target;

    protected boolean recalculatePath = true;

    //Queue of nodes to follow
    protected final Queue<PathNode> pathQueue = new Queue<>();
    private PathNode currentPos;
    private PathNode newGoal;

    protected DistancerInstance(TexturedEntity entity, GameInstance target) {
        super(entity);
        this.target = target;
        this.targetPathNode = new PathNode(target.getBody().getPosition());
    }

    /**
     * Set the goal PathNode, calculate a path, and start moving.
     */
    public void calculateNewGoal(RoomNodesGraph roomNodesGraph) {

        //Do not calculate if has no nodesGraph or if target is in line of sight
        if (!recalculatePath || Objects.isNull(roomNodesGraph))
            return;

        //Get a subarray of only the nearest nodes to the DistancerInstance to improve the escape
        final Array<PathNode> nodeArray = roomNodesGraph.getNodeArray();
        Array<PathNode> nearestNodes = new Array<>();
        for (PathNode n : nodeArray) {
            if (n.dst(body.getPosition()) <= DISTANCE_LIMIT_TO_EVALUATE) {
                nearestNodes.add(n);
            }
        }

        //get nearest to This
        nearestNodes.sort((n1, n2) -> Float.compare(body.getPosition().dst(n1), body.getPosition().dst(n2)));
        currentPos = nearestNodes.get(0);

        //get farthest to Target
        nearestNodes.sort((n1, n2) -> Float.compare(target.getBody().getPosition().dst(n2), target.getBody().getPosition().dst(n1)));
        newGoal = nearestNodes.get(0);

        final GraphPath<PathNode> graphPath = PathfinderUtils.generatePath(currentPos, newGoal, roomNodesGraph);
        for (PathNode pathNode : graphPath) {
            pathQueue.addLast(pathNode);

            //Limit queue size
            if(pathQueue.size >= 3){
                break;
            }
        }
        recalculatePath = pathQueue.isEmpty();
        targetPathNode = pathQueue.isEmpty() ? currentPos : pathQueue.removeFirst();
    }

    /**
     * Check if should go to target or another point
     *
     * @return
     */
    protected Vector2 getMovementDestination() {

        //If node is reached
        if (body.getPosition().dst(targetPathNode) <= 5) {
            if (pathQueue.isEmpty()) {
                //If path is finished, recalculate
                recalculatePath = true;
            } else {
                //Else go to next node in path
                targetPathNode = pathQueue.removeFirst();
            }
        }

        return targetPathNode;
    }
}
