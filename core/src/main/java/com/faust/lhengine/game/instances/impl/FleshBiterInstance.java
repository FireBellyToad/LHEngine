package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.impl.FleshBiterEntity;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.manager.CollisionManager;

import java.util.Objects;

/**
 * Flesh biter enemy instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class FleshBiterInstance extends AnimatedInstance implements Interactable, Damager, Killable {

    private static final float MEAT_SPEED = 65;

    private final Vector2 target; // Target x and y;
    private long startAttackCooldown = 0;

    public FleshBiterInstance(float x, float y, PlayerInstance playerInstance, AssetManager assetManager) {
        super(new FleshBiterEntity(assetManager));
        currentDirectionEnum = DirectionEnum.DOWN;
        this.startX = x;
        this.startY = y;

        target = playerInstance.getBody().getPosition().cpy();
        changeCurrentBehavior(GameBehavior.WALK);
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        switch (getCurrentBehavior()) {
            case ATTACK: {
                if (TimeUtils.timeSinceNanos(startAttackCooldown) > TimeUtils.millisToNanos(3000)) {
                    changeCurrentBehavior(GameBehavior.DEAD);
                    dispose();
                }
                break;
            }
            case WALK: {
                Vector2 direction = new Vector2(target.x - body.getPosition().x, target.y - body.getPosition().y).nor();

                currentDirectionEnum = extractDirectionFromNormal(direction);

                // Move towards target
                body.setLinearVelocity(MEAT_SPEED * direction.x, MEAT_SPEED * direction.y);

                // If near target, starts attacking
                if (target.dst(getBody().getPosition()) < 1) {
                    changeCurrentBehavior(GameBehavior.ATTACK);
                    startAttackCooldown = TimeUtils.nanoTime();
                    body.setLinearVelocity(0, 0);
                }
                break;
            }
            default: {
                throw new GdxRuntimeException("Unexpected MeatInstance behaviour!");
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
        Vector2 drawPosition = adjustPosition();
        TextureRegion frame = ((AnimatedEntity) entity).getFrame(getCurrentBehavior(), mapStateTimeFromBehaviour(stateTime), true);
        batch.draw(frame, drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);
        batch.end();
    }

    @Override
    public boolean isDisposable() {
        return isDead();
    }

    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {
        // Bounce player away
        playerInstance.hurt(this);
    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        // Nothing to do here... yet
    }

    public double damageRoll() {
        return 2;
    }

    private float mapStateTimeFromBehaviour(float stateTime) {
        return stateTime * 0.75f;
    }

    @Override
    public boolean isDying() {
        //is never dying...
        return false;
    }

    @Override
    public boolean isDead() {
        return GameBehavior.DEAD.equals(getCurrentBehavior());
    }
}
