package com.faust.lhengine.game.world.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.instances.impl.ScriptActorInstance;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.areas.TriggerArea;
import com.faust.lhengine.game.world.interfaces.RayCaster;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.impl.DecorationInstance;
import com.faust.lhengine.game.instances.impl.POIInstance;
import com.faust.lhengine.game.rooms.areas.EmergedArea;
import com.faust.lhengine.game.rooms.areas.WallArea;

import java.util.List;
import java.util.Objects;


/**
 * Wraps Box2D world
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class WorldManager implements RayCaster {

    private static final float DEFAULT_TIME_STEP = 1 / 60f;
    private static final int DEFAULT_VELOCITY_ITERATIONS = 8;
    private static final int DEFAULT_POSITION_ITERATIONS = 3;

    private final World world;
    private float accumulator;

    public WorldManager() {
        this.world = new World(new Vector2(0, 0), true);
        world.setContactListener(new CollisionManager());
    }

    /**
     * Makes the world step to next. Handles also correct physics speed
     * @param deltaTime
     */
    public void doStep(float deltaTime) {
        // Thanks to Lyze of Discord LibGDX community!!!
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.125f);
        accumulator += frameTime;
        while (accumulator >= DEFAULT_TIME_STEP) {
            world.step(DEFAULT_TIME_STEP,DEFAULT_VELOCITY_ITERATIONS,DEFAULT_POSITION_ITERATIONS);
            accumulator -= DEFAULT_TIME_STEP;
        }

    }

    public void dispose() {
        world.dispose();
    }

    /**
     * Inserts a PlayerInstance into Box2D World
     *
     * @param playerInstance
     * @param x
     * @param y
     */
    public void insertPlayerIntoWorld(final PlayerInstance playerInstance, float x, float y) {
        Objects.requireNonNull(playerInstance);

        Float horizontalVelocity = null;
        Float verticalVelocity = null;

        // If player has already a linear velocity, restore after body
        if (!Objects.isNull(playerInstance.getBody())) {
            horizontalVelocity = playerInstance.getBody().getLinearVelocity().x;
            verticalVelocity = playerInstance.getBody().getLinearVelocity().y;
        }

        // Insert into world generating new body
        this.insertIntoWorld(playerInstance, x, y);

        playerInstance.setStartX(0);
        playerInstance.setStartY(0);

        if (!Objects.isNull(verticalVelocity)) {
            playerInstance.getBody().setLinearVelocity(horizontalVelocity, verticalVelocity);
        }
    }

    /**
     * Inserts a GameInstance into Box2D World
     *
     * @param instance the instance to insert
     * @param x
     * @param y
     */
    private void insertIntoWorld(final GameInstance instance, float x, float y) {
        Objects.requireNonNull(instance);
        instance.createBody(this.world, x, y);
    }

    /**
     * Insert a list of POI into world, in random positions
     *
     * @param poiList
     */
    public void insertPOIIntoWorld(final List<POIInstance> poiList) {
        Objects.requireNonNull(poiList);

        poiList.forEach((poi) -> this.insertIntoWorld(poi, poi.getStartX(), poi.getStartY()));
    }

    /**
     * Insert a list of Decorations into world, in random positions
     *
     * @param decorationInstances
     */
    public void insertDecorationsIntoWorld(List<DecorationInstance> decorationInstances) {
        Objects.requireNonNull(decorationInstances);

        decorationInstances.forEach((deco) -> this.insertIntoWorld(deco, deco.getStartX(), deco.getStartY()));
    }

    /**
     * Destroy all bodies currently in Box2D world
     */
    public void clearBodies() {
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        bodies.forEach(this.world::destroyBody);
    }

    /**
     * Insert a list of Enemies into world
     *
     * @param enemiesInstance
     */
    public void insertEnemiesIntoWorld(List<AnimatedInstance> enemiesInstance) {
        Objects.requireNonNull(enemiesInstance);

        enemiesInstance.forEach((e) -> this.insertIntoWorld(e, e.getStartX(), e.getStartY()));
    }

    /**
     * Insert static walls into world
     *
     * @param wallList
     */
    public void insertWallsIntoWorld(List<WallArea> wallList) {
        Objects.requireNonNull(wallList);

        wallList.forEach((w) -> w.createBody(this.world));
    }

    /**
     * Insert static emerged areas into world
     *
     * @param areasList
     */
    public void insertEmergedAreasIntoWorld(List<EmergedArea> areasList) {
        Objects.requireNonNull(areasList);

        areasList.forEach((a) -> a.createBody(this.world));
    }

    /**
     * @param echoActors
     */
    public void insertEchoActorsIntoWorld(List<ScriptActorInstance> echoActors) {
        Objects.requireNonNull(echoActors);

        echoActors.forEach((a) -> a.createBody(this.world, a.getStartX(), a.getStartY()));
    }

    /**
     * Insert a list of Spells into world
     *
     * @param spellInstances
     */
    public void insertSpellsIntoWorld(List<GameInstance> spellInstances) {
        Objects.requireNonNull(spellInstances);

        spellInstances.forEach((s) -> this.insertIntoWorld(s, s.getStartX(), s.getStartY()));
    }

    /**
     * @param triggers
     */
    public void insertTriggersIntoWorld(List<TriggerArea> triggers) {
        Objects.requireNonNull(triggers);

        triggers.forEach((a) -> a.createBody(this.world));
    }


    @Override
    public void rayCast(RayCastCallback callback, Vector2 from, Vector2 to) {
        world.rayCast(callback,from,to);
    }

    public World getWorld() {
        return world;
    }
}
