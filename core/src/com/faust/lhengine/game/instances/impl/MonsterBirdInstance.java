package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.impl.MonsterBirdEntity;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.ChaserInstance;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Hurtable;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.interfaces.RayCaster;
import com.faust.lhengine.game.world.manager.CollisionManager;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.Objects;

/**
 * Strix enemy instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MonsterBirdInstance extends ChaserInstance implements Interactable, Hurtable, Damager {

    private static final float SPEED = 35;
    private static final long LEECHING_FREQUENCY_IN_MILLIS = 1000;
    private boolean attachedToPlayer = false;

    private long leechStartTimer;

    public MonsterBirdInstance(float x, float y, PlayerInstance target, AssetManager assetManager, RayCaster rayCaster) {
        super(new MonsterBirdEntity(assetManager), target, rayCaster);
        currentDirectionEnum = DirectionEnum.DOWN;
        this.startX = x;
        this.startY = y;
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        hitBox.setTransform(body.getPosition().x, body.getPosition().y + 8, 0);

        if (GameBehavior.HURT.equals(getCurrentBehavior()) || GameBehavior.DEAD.equals(getCurrentBehavior()))
            return;

        if (!attachedToPlayer && (canSeeTarget() || isAggressive)) {
            changeCurrentBehavior(GameBehavior.WALK);
            isAggressive = true;
            calculateNewGoal(roomContent.roomGraph);

            // Normal from strix position to target
            final Vector2 destination = getMovementDestination();
            Vector2 direction = new Vector2(destination.x - body.getPosition().x,
                    destination.y - body.getPosition().y).nor();

            // If not already attached su player
            currentDirectionEnum = extractDirectionFromNormal(direction);

            // Move towards target
            body.setLinearVelocity(SPEED * direction.x, SPEED * direction.y);
        } else {

            changeCurrentBehavior(GameBehavior.IDLE);
            body.setLinearVelocity(0, 0);

            //leech if attachedToPlayer
            if (attachedToPlayer) {
                changeCurrentBehavior(GameBehavior.ATTACK);
                leechLife();
            } else {
                leechStartTimer = 0;
            }
        }
    }

    private float mapStateTimeFromBehaviour(float stateTime) {
        return stateTime;
    }

    @Override
    public void postHurtLogic(GameInstance attacker) {

        //If for some reason is not aggressive, force it after being hurt
        this.isAggressive = true;

        // is pushed away while flickering
        Vector2 direction = new Vector2(attacker.getBody().getPosition().x - body.getPosition().x,
                attacker.getBody().getPosition().y - body.getPosition().y).nor();

        body.setLinearVelocity(SPEED * 4 * -direction.x, SPEED * 4 * -direction.y);
        changeCurrentBehavior(GameBehavior.HURT);
        attachedToPlayer = false;
        // Do nothing for half second
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                changeCurrentBehavior(GameBehavior.IDLE);
            }
        }, 0.25f);
    }


    /**
     * @return true if the damage is greater or equal than the resitance
     */
    @Override
    public boolean isDying() {
        return this.damage >= getResistance();
    }

    @Override
    public boolean isDead() {
        return GameBehavior.DEAD.equals(getCurrentBehavior());
    }

    @Override
    public double damageRoll() {
        return 1;
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
        shape.setAsBox(4, 2);

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0;
        fixtureDef.friction = 0;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CollisionManager.ENEMY_GROUP;
        fixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        shape.dispose();

        // Hitbox definition
        BodyDef hitBoxDef = new BodyDef();
        hitBoxDef.type = BodyDef.BodyType.DynamicBody;
        hitBoxDef.fixedRotation = true;
        hitBoxDef.position.set(x, y);

        // Define shape
        PolygonShape hitBoxShape = new PolygonShape();
        hitBoxShape.setAsBox(4, 6);

        // Define Fixture
        FixtureDef hitBoxFixtureDef = new FixtureDef();
        hitBoxFixtureDef.shape = hitBoxShape;
        hitBoxFixtureDef.density = 0;
        hitBoxFixtureDef.friction = 0;
        hitBoxFixtureDef.filter.categoryBits = CollisionManager.ENEMY_GROUP;
        hitBoxFixtureDef.filter.maskBits = CollisionManager.WEAPON_GROUP;

        // Associate body to world
        hitBox = world.createBody(hitBoxDef);
        hitBox.setUserData(this);
        hitBox.createFixture(hitBoxFixtureDef);

        hitBoxShape.dispose();
    }

    /**
     * Draw the Entity frames using Body position
     *
     * @param batch
     * @param stateTime
     */
    public void draw(final SpriteBatch batch, float stateTime) {
        Objects.requireNonNull(batch);

        Vector2 drawPosition = adjustPosition();
        TextureRegion frame = ((AnimatedEntity) entity).getFrame(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime));
        batch.begin();
        //Draw shadow
        batch.draw(((MonsterBirdEntity) entity).getShadowTexture(), drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);

        //Draw Strix
        if (GameBehavior.IDLE.equals(getCurrentBehavior()) || GameBehavior.DEAD.equals(getCurrentBehavior())) {
            // On Idle, the Strix is landed. While walking it flies
            batch.draw(frame, drawPosition.x - POSITION_OFFSET, drawPosition.y - 8 - POSITION_Y_OFFSET);
        } else {

            // If not hurt or the flickering POI must be shown, draw the texture
            if (!mustFlicker || !GameBehavior.HURT.equals(getCurrentBehavior())) {
                batch.draw(frame, drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);
            }

            // Every 1/8 seconds alternate between showing and hiding the texture to achieve flickering effect
            if (GameBehavior.HURT.equals(getCurrentBehavior()) && TimeUtils.timeSinceNanos(startToFlickTime) > GameScreen.FLICKER_DURATION_IN_NANO / 6) {
                mustFlicker = !mustFlicker;

                // restart flickering timer
                startToFlickTime = TimeUtils.nanoTime();
            }
        }
        batch.end();

    }

    @Override
    public boolean isDisposable() {
        return isDead();
    }

    @SuppressWarnings("unchecked")
    private void leechLife() {

        // Force cancel another one must start
        if (leechStartTimer == 0) {
            leechStartTimer = TimeUtils.nanoTime();
        }

        //Keep leeching
        if (attachedToPlayer && TimeUtils.timeSinceNanos(leechStartTimer) > TimeUtils.millisToNanos(LEECHING_FREQUENCY_IN_MILLIS)) {

            ((Hurtable<MonsterBirdInstance>) target).hurt(MonsterBirdInstance.this);
            //Prevents loop on gameover screen
            if (((Killable) target).isDead()) {
                ((MonsterBirdEntity) entity).stopLeechSound();
            }
            leechStartTimer = TimeUtils.nanoTime();
            Gdx.app.log(LoggerUtils.DEBUG_TAG, "END leech timer");
        }
        Gdx.app.log(LoggerUtils.DEBUG_TAG, "START leech timer");

    }

    public boolean isAttachedToPlayer() {
        return attachedToPlayer;
    }

    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {
        // Start to leech
        attachedToPlayer = true;
        ((MonsterBirdEntity) entity).playLeechSound();

    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        // End leech and cancel timer if present
        attachedToPlayer = false;
        ((MonsterBirdEntity) entity).stopLeechSound();
        if (leechStartTimer > 0) {
            leechStartTimer = 0;
            Gdx.app.log(LoggerUtils.DEBUG_TAG, "CANCEL leech timer");
        }
    }

    /**
     * Method for hurting the Strix
     *
     * @param attacker
     */
    @Override
    public void hurt(GameInstance attacker) {
        Objects.requireNonNull(attacker);

        //Should not be hurted if attached to player!
        if (!isAttachedToPlayer()) {

            if (isDying()) {
                ((MonsterBirdEntity) entity).playDeathCry();
                body.setLinearVelocity(0, 0);
                changeCurrentBehavior(GameBehavior.DEAD);
            } else if (!GameBehavior.HURT.equals(getCurrentBehavior())) {
                ((MonsterBirdEntity) entity).playHurtCry();
                // Hurt by player
                this.damage += ((Damager) attacker).damageRoll();
                Gdx.app.log(LoggerUtils.DEBUG_TAG, "Instance " + this.getClass().getSimpleName() + " total damage " + damage);
                postHurtLogic(attacker);
            }
        }
    }

    @Override
    public int getResistance() {
        return 5;
    }
}
