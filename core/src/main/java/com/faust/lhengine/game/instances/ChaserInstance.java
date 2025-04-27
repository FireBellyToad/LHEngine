package com.faust.lhengine.game.instances;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.faust.lhengine.game.ai.PathNode;
import com.faust.lhengine.game.ai.RoomNodesGraph;
import com.faust.lhengine.game.gameentities.TexturedEntity;
import com.faust.lhengine.game.world.interfaces.RayCaster;
import com.faust.lhengine.utils.Pair;
import com.faust.lhengine.utils.PathfinderUtils;
import com.faust.lhengine.utils.RayCastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Smart chaser instance class, used on things that should follow a target smartly:
 * - if can see him, just follow him
 * - if cannot see him, then do A* in nodegraph
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public abstract class ChaserInstance extends AnimatedInstance {

    protected final RayCaster rayCaster;

    //Current path node to follow
    protected PathNode targetPathNode;
    //Target instance
    protected final GameInstance target;

    protected boolean isAggressive = false;
    protected boolean recalculatePath = true;

    //Queue of nodes to follow
    protected final Queue<PathNode> pathQueue = new Queue<>();
    private PathNode currentPos;
    private PathNode newGoal;

    protected ChaserInstance(TexturedEntity entity, GameInstance target, RayCaster rayCaster) {
        super(entity);
        this.target = target;
        this.rayCaster = rayCaster;
        this.targetPathNode = new PathNode(target.getBody().getPosition());
    }

    /**
     * Set the goal PathNode, calculate a path, and start moving.
     */
    public void calculateNewGoal(RoomNodesGraph roomNodesGraph) {

        //Do not calculate if has no nodesGraph or if target is in line of sight
        if (!recalculatePath || Objects.isNull(roomNodesGraph) || canSeeTarget())
            return;

        final Array<PathNode> nodeArray = roomNodesGraph.getNodeArray();

        //get nearest to This
        nodeArray.sort((n1, n2) -> Float.compare(body.getPosition().dst(n1), body.getPosition().dst(n2)));
        currentPos = nodeArray.get(0);

        //get nearest to Target
        nodeArray.sort((n1, n2) -> Float.compare(target.getBody().getPosition().dst(n1), target.getBody().getPosition().dst(n2)));
        newGoal = nodeArray.get(0);

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
     *
     * @return true if target can be seen by this instance
     */
    protected boolean canSeeTarget() {
        //Check if target is in range
        if ((getDistanceFromTarget() > (getLineOfSight() * 0.75) && !isAggressive) ||
                (getDistanceFromTarget() > getLineOfSight() && isAggressive)) {
            return false;
        }

        //Do a raycast, save all instances that are caught by the ray
        final List<Pair<Float, Object>> tempInstancesList = new ArrayList<>();
        final RayCastCallback getTargetAndWallsInRay = (fixture, point, normal, fraction) -> {
            //Select only walls, non passable decorations and target (excluding hitboxes)
            if (RayCastUtils.isTargetOrWall(fixture, target)) {
                tempInstancesList.add(new Pair<>(fraction, fixture.getBody().getUserData()));
            }
            return 1;
        };

        rayCaster.rayCast(getTargetAndWallsInRay, body.getPosition(), target.getBody().getPosition());
        //Order by distance from this, using fraction
        tempInstancesList.sort((p1, p2) -> Float.compare(p1.getFirst(), p2.getFirst()));
        //Check if first one is target
        return !tempInstancesList.isEmpty() && tempInstancesList.get(0).getSecond().equals(target);
    }

    /**
     * Can be overridden
     *
     * @return maximum line of sight
     */
    protected float getLineOfSight() {
        return 60f;
    }

    /**
     * Can be overridden
     *
     * @return Distance From target instance
     */
    protected float getDistanceFromTarget() {
        return target.getBody().getPosition().dst(body.getPosition());
    }

    /**
     * Check if should go to target instance or another point
     *
     * @return
     */
    protected Vector2 getMovementDestination() {

        if (canSeeTarget()) {
            //If can see target, just follow
            isAggressive = true;
            recalculatePath = true;
            pathQueue.clear();
            return target.getBody().getPosition();
        } else {

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
        }
        return targetPathNode;
    }

    /**
     * FIXME REMOVE
     */
//    public void drawDebug(OrthographicCamera cameraTemp) {
//
//        ShapeRenderer shapeRenderer = new ShapeRenderer();
//        Color back = new Color(0xffffffff);
//
//        PathNode previous = null;
//        for (PathNode p : pathQueue) {
//
//            if (Objects.isNull(previous)) {
//                previous = p;
//                continue;
//            }
//            shapeRenderer.setColor(back);
//            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.line(p, previous);
//            shapeRenderer.end();
//            previous = p;
//        }
//
//        if (Objects.nonNull(currentPos)) {
//
//            shapeRenderer.setColor(back);
//            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.circle(currentPos.x, currentPos.y, 5);
//            shapeRenderer.end();
//        }
//        if (Objects.nonNull(newGoal)) {
//
//            shapeRenderer.setColor(back);
//            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.circle(newGoal.x, newGoal.y, 5);
//            shapeRenderer.end();
//        }
//    }

    /**
     * Force this instance to use graph
     */
    public void forceRecalculation() {
        recalculatePath = true;
    }
}
