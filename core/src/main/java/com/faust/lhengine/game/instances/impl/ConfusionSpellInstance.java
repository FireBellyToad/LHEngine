package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.enums.PlayerFlag;
import com.faust.lhengine.game.gameentities.impl.ParticleEffectEntity;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.manager.CollisionManager;

import java.util.Objects;

/**
 * Confusion spell instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ConfusionSpellInstance extends GameInstance implements Interactable, Damager, Killable {

    private final PlayerInstance target; // Target x and y;

    private GameBehavior currentBehavior;
    private long attackTimer = 0;

    public ConfusionSpellInstance(float x, float y, PlayerInstance playerInstance) {
        super(new ParticleEffectEntity("confusion_spell"));
        this.startX = x;
        this.startY = y;

        target = playerInstance;
        currentBehavior = GameBehavior.WALK;
        ((ParticleEffectEntity) entity).getParticleEffect().start();
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        if (isDead())
            return;

        final Vector2 targetPosition = target.getBody().getPosition();

        //Translate emitter
        ((ParticleEffectEntity) entity).getParticleEffect().setPosition(body.getPosition().x, body.getPosition().y);

        switch (currentBehavior) {
            case ATTACK: {
                if (TimeUtils.timeSinceNanos(attackTimer) > TimeUtils.millisToNanos(1000)) {
                    currentBehavior = GameBehavior.DEAD;
                    //FIXME workaround because collision only in attack behavior is not working!
                    if(this.body.getPosition().dst(targetPosition) < 5){
                        target.setPlayerFlagValue(PlayerFlag.IS_CONFUSED,true);
                    }
                    dispose();
                }
                break;
            }
            case IDLE: {
                body.setLinearVelocity(0, 0);

                // Count to 1 seconds
                if (TimeUtils.timeSinceNanos(attackTimer) > TimeUtils.millisToNanos(1000)) {
                    // after 1 seconds, explode
                    currentBehavior = GameBehavior.ATTACK;
                    attackTimer = TimeUtils.nanoTime();
                }
                break;
            }
            case WALK: {
                // Overlap and keep on target
                body.setTransform(targetPosition, 0);

                // Count to 2 seconds
                if (attackTimer == 0) {
                    attackTimer = TimeUtils.nanoTime();
                } else {
                    // after 2 seconds, stop
                    if (TimeUtils.timeSinceNanos(attackTimer) > TimeUtils.millisToNanos(2000)) {
                        currentBehavior = GameBehavior.ATTACK;
                        attackTimer = TimeUtils.nanoTime();
                    }
                }
                break;
            }
            default: {
                throw new GdxRuntimeException("Unexpected ConfusionSpellInstance behaviour!");
            }
        }
    }

    @Override
    public void createBody(World world, float x, float y) {

        Objects.requireNonNull(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);

        // Define shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8, 2);

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;
        fixtureDef.friction = 1;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CollisionManager.ENEMY_GROUP;
        fixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);
        shape.dispose();

    }

    /**
     * Draw the Entity frames using Body position
     *
     * @param batch
     * @param stateTime
     */
    public void draw(final SpriteBatch batch, float stateTime) {
        Objects.requireNonNull(batch);
        batch.begin();
        ((ParticleEffectEntity) entity).getParticleEffect().draw(batch, Gdx.graphics.getDeltaTime());
        batch.end();
    }

    @Override
    public boolean isDisposable() {
        return isDead();
    }

    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {
        // Nothing to do here... yet
    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        // Nothing to do here... yet
    }

    public double damageRoll() {
        return 2;
    }

    @Override
    public boolean isDying() {
        //cannot ever be in dying state...
        return false;
    }

    /**
     * @return true if outside of screen
     */
    @Override
    public boolean isDead() {
        return GameBehavior.DEAD.equals(currentBehavior);
    }
}
