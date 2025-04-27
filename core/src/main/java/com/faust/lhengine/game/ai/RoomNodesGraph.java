package com.faust.lhengine.game.ai;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.interfaces.RayCaster;
import com.faust.lhengine.utils.RayCastUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Path node Graph class, used in a single Room
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class RoomNodesGraph implements IndexedGraph<PathNode> {

    //Container lists
    private final Array<PathNode> nodeArray = new Array<>();
    private final Array<Path> pathArray = new Array<>();

    // Mapping all Paths starting from a PathNode
    final Map<PathNode, Array<Path>> graphMap = new HashMap<>();

    public void addPathNode(PathNode pathNode) {
        pathNode.index = nodeArray.size;
        nodeArray.add(pathNode);
    }

    /**
     * Calculate graph
     */
    public void initGraph(RayCaster rayCaster) {
        //Raycast search callback. If at least one StaticBody is in the line of the ray,
        //the path should not be saved for pathfinding. This excludes EmergedArea bodies
        //Why AtomicBoolean? So that it can be changed on callback thread
        AtomicBoolean isConnected = new AtomicBoolean(true);
        RayCastCallback checkIfPathIsFree = (fixture, point, normal, fraction) -> {
            if (RayCastUtils.isNotPassable(fixture)) {
                isConnected.set(false);
            }
            return isConnected.get() ? 1 : 0;
        };

        //For each node, check if thers is a connection with another one
        for (PathNode fromNode : nodeArray) {
            for (int i = 0; i < nodeArray.size; i++) {
                PathNode toNode = nodeArray.get(i);

                //Skip same node
                if (fromNode.equals(toNode)) {
                    continue;
                }

                //init check flag
                isConnected.set(true);

                //If connection is free, connect two nodes
                rayCaster.rayCast(checkIfPathIsFree, fromNode, toNode);
                if (isConnected.get()) {
                    connectNodes(fromNode, toNode);
                }
            }
        }
    }

    /**
     * Add to Graph a new Path
     *
     * @param fromPathNode
     * @param toPathNode
     */
    public void connectNodes(PathNode fromPathNode, PathNode toPathNode) {

        final Path newPath = new Path(fromPathNode, toPathNode);
        //Create if not present
        if (!graphMap.containsKey(fromPathNode)) {
            graphMap.put(fromPathNode, new Array<>());
        }
        graphMap.get(fromPathNode).add(newPath);
        pathArray.add(newPath);
    }

    @Override
    public int getIndex(PathNode node) {
        return node.index;
    }

    @Override
    public int getNodeCount() {
        return nodeArray.size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Array<Connection<PathNode>> getConnections(PathNode fromNode) {
        final Array value = graphMap.getOrDefault(fromNode, null);
        //remember that Path implements Connection<PathNode>
        return Objects.isNull(value) ? new Array<>() : value;
    }

    public Array<PathNode> getNodeArray() {
        return nodeArray;
    }

    //FIXME remove
    public void debugDraw(OrthographicCamera cameraTemp, RoomContent roomContent, SpriteBatch batch, AssetManager assetManager) {


        ShapeRenderer shapeRenderer = new ShapeRenderer();
        Color back = new Color(0x222222ff);
        Color back2 = new Color(0x666666ff);
        for (PathNode node : nodeArray) {
            batch.begin();
            shapeRenderer.setColor(back);
            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.circle(node.x, node.y, 2);
            shapeRenderer.end();
            batch.end();


            //Text
            batch.begin();
            BitmapFont mainFont = assetManager.get("fonts/main_font.fnt");
            mainFont.draw(batch, String.valueOf(node.index), node.x - 2, node.y - 2);
            batch.end();
        }

        batch.begin();
        for (Path p : pathArray) {
            shapeRenderer.setColor(back);
            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.line(p.fromPathNode, p.toPathNode);
            shapeRenderer.end();
        }
        graphMap.forEach((k, v) -> {
            for (Path p : pathArray) {
                shapeRenderer.setColor(back2);
                shapeRenderer.setProjectionMatrix(cameraTemp.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.line(p.fromPathNode, p.toPathNode);
                shapeRenderer.end();
            }
        });


        for (AnimatedInstance ene : roomContent.enemyList) {
            shapeRenderer.setColor(back);
            shapeRenderer.setProjectionMatrix(cameraTemp.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.line(ene.getBody().getPosition(),
                    roomContent.player.getBody().getPosition());
            shapeRenderer.end();
        }

        batch.end();
    }
}
