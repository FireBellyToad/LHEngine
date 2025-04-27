package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.instances.ChaserInstance;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.interfaces.RayCaster;
import com.faust.lhengine.game.world.manager.CollisionManager;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.impl.WillowispEntity;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Hurtable;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.Objects;

/**
 * Will' o wisp enemy instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class WillowispInstance extends ChaserInstance implements Interactable, Hurtable, Damager {

    private static final float WILLOWISP_SPEED = 35;
    private static final int LINE_OF_ATTACK = 15;
    private static final float TENTACLE_SENSOR_Y_OFFSET = 10;
    private static final int ATTACK_VALID_FRAME = 2; // Frame to activate attack sensor
    private static final long ATTACK_COOLDOWN_TIME = 850; // in millis

    // Time delta between state and start of attack animation
    private float attackDeltaTime = 0;
    private long startAttackCooldown = 0;

    //Body for spear attacks
    private Body downTentacleBody;
    private Body leftTentacleBody;
    private Body rightTentacleBody;
    private Body upTentacleBody;

    public WillowispInstance(float x, float y, PlayerInstance target, AssetManager assetManager, RayCaster rayCaster) {
        super(new WillowispEntity(assetManager),target,rayCaster);
        currentDirectionEnum = DirectionEnum.RIGHT;
        this.startX = x;
        this.startY = y;
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        translateAccessoryBodies();

        if (GameBehavior.EVADE.equals(getCurrentBehavior()) || GameBehavior.HURT.equals(getCurrentBehavior()) || GameBehavior.DEAD.equals(getCurrentBehavior()))
            return;

        //Try to attack if not dead. If is attacking and is too far away, still needs to end the attack before following
        //the player
        if (!((Killable)target).isDead() && TimeUtils.timeSinceNanos(startAttackCooldown) > TimeUtils.millisToNanos(ATTACK_COOLDOWN_TIME) && (GameBehavior.IDLE.equals(getCurrentBehavior()) ||
                target.getBody().getPosition().dst(getBody().getPosition()) <= LINE_OF_ATTACK)) {

            //Wait in visible IDLE before attacking
            if (!GameBehavior.IDLE.equals(getCurrentBehavior()) && !GameBehavior.ATTACK.equals(getCurrentBehavior())) {
                changeCurrentBehavior(GameBehavior.IDLE);
                attackDeltaTime = stateTime;
                body.setLinearVelocity(0, 0);
            } else if (GameBehavior.IDLE.equals(getCurrentBehavior()) &&
                    ((WillowispEntity) entity).isAnimationFinished(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime))) {
                // Start attacking on IDLE end
                attackDeltaTime = stateTime;
                changeCurrentBehavior(GameBehavior.ATTACK);
            } else if (GameBehavior.ATTACK.equals(getCurrentBehavior())) {
                //Start attack animation
                // Normal from flying_corpse position to target
                Vector2 direction = new Vector2(target.getBody().getPosition().x - body.getPosition().x,
                        target.getBody().getPosition().y - body.getPosition().y).nor();
                currentDirectionEnum = extractDirectionFromNormal(direction);

                attackLogic(stateTime);
                body.setLinearVelocity(0, 0);
            }

        } else if (target.getBody().getPosition().dst(getBody().getPosition()) > LINE_OF_ATTACK) {
            calculateNewGoal(roomContent.roomGraph);
            deactivateAttackBodies();
            changeCurrentBehavior(GameBehavior.WALK);
            // Normal from Willowisp position to target
            final Vector2 destination = getMovementDestination();
            Vector2 direction = new Vector2(destination.x - body.getPosition().x,
                    destination.y - body.getPosition().y).nor();

            currentDirectionEnum = extractDirectionFromNormal(direction);

            // Move towards target
            body.setLinearVelocity(WILLOWISP_SPEED * direction.x, WILLOWISP_SPEED * direction.y);
            deactivateAttackBodies();
        }
    }

    /**
     * Translate all accessory body
     */
    private void translateAccessoryBodies() {
        rightTentacleBody.setTransform(body.getPosition().x + 10, body.getPosition().y + TENTACLE_SENSOR_Y_OFFSET, 0);
        upTentacleBody.setTransform(body.getPosition().x, body.getPosition().y + 11 + TENTACLE_SENSOR_Y_OFFSET, 0);
        leftTentacleBody.setTransform(body.getPosition().x - 10, body.getPosition().y + TENTACLE_SENSOR_Y_OFFSET, 0);
        downTentacleBody.setTransform(body.getPosition().x, body.getPosition().y - 11 + TENTACLE_SENSOR_Y_OFFSET, 0);
        hitBox.setTransform(body.getPosition().x, body.getPosition().y + 8, 0);
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
        fixtureDef.filter.categoryBits = CollisionManager.ENEMY_GROUP;
        fixtureDef.filter.maskBits = CollisionManager.SOLID_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);
        shape.dispose();

        BodyDef rightClawDef = new BodyDef();
        rightClawDef.type = BodyDef.BodyType.KinematicBody;
        rightClawDef.fixedRotation = true;
        rightClawDef.position.set(x + 2, y);

        // Define shape
        PolygonShape rightClawShape = new PolygonShape();
        rightClawShape.setAsBox(4, 6);

        // Define Fixtures
        FixtureDef rightClawFixtureDef = new FixtureDef();
        rightClawFixtureDef.shape = rightClawShape;
        rightClawFixtureDef.density = 1;
        rightClawFixtureDef.friction = 1;
        rightClawFixtureDef.isSensor = true;
        rightClawFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        rightClawFixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        rightTentacleBody = world.createBody(rightClawDef);
        rightTentacleBody.setUserData(this);
        rightTentacleBody.createFixture(rightClawFixtureDef);
        rightTentacleBody.setActive(false);
        rightClawShape.dispose();

        BodyDef upClawDef = new BodyDef();
        upClawDef.type = BodyDef.BodyType.KinematicBody;
        upClawDef.fixedRotation = true;
        upClawDef.position.set(x, y - 2);

        // Define shape
        PolygonShape upClawShape = new PolygonShape();
        upClawShape.setAsBox(6, 4);

        // Define Fixtures
        FixtureDef upClawFixtureDef = new FixtureDef();
        upClawFixtureDef.shape = upClawShape;
        upClawFixtureDef.density = 1;
        upClawFixtureDef.friction = 1;
        upClawFixtureDef.isSensor = true;
        upClawFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        upClawFixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        upTentacleBody = world.createBody(upClawDef);
        upTentacleBody.setUserData(this);
        upTentacleBody.createFixture(upClawFixtureDef);
        upTentacleBody.setActive(false);
        upClawShape.dispose();

        BodyDef leftClawDef = new BodyDef();
        leftClawDef.type = BodyDef.BodyType.KinematicBody;
        leftClawDef.fixedRotation = true;
        leftClawDef.position.set(x - 2, y);

        // Define shape
        PolygonShape leftClawShape = new PolygonShape();
        leftClawShape.setAsBox(4, 6);

        // Define Fixtures
        FixtureDef leftClawFixtureDef = new FixtureDef();
        leftClawFixtureDef.shape = leftClawShape;
        leftClawFixtureDef.density = 1;
        leftClawFixtureDef.friction = 1;
        leftClawFixtureDef.isSensor = true;
        leftClawFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        leftClawFixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        leftTentacleBody = world.createBody(leftClawDef);
        leftTentacleBody.setUserData(this);
        leftTentacleBody.createFixture(leftClawFixtureDef);
        leftTentacleBody.setActive(false);
        leftClawShape.dispose();

        BodyDef downClawDef = new BodyDef();
        downClawDef.type = BodyDef.BodyType.KinematicBody;
        downClawDef.fixedRotation = true;
        downClawDef.position.set(x, y + 2);

        // Define shape
        PolygonShape downClawShape = new PolygonShape();
        downClawShape.setAsBox(6, 4);

        // Define Fixtures
        FixtureDef downClawFixtureDef = new FixtureDef();
        downClawFixtureDef.shape = downClawShape;
        downClawFixtureDef.density = 1;
        downClawFixtureDef.friction = 1;
        downClawFixtureDef.isSensor = true;
        downClawFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        downClawFixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        downTentacleBody = world.createBody(downClawDef);
        downTentacleBody.setUserData(this);
        downTentacleBody.createFixture(downClawFixtureDef);
        downTentacleBody.setActive(false);
        downClawShape.dispose();

        // Hitbox definition
        BodyDef hitBoxDef = new BodyDef();
        hitBoxDef.type = BodyDef.BodyType.DynamicBody;
        hitBoxDef.fixedRotation = true;
        hitBoxDef.position.set(x, y);

        // Define shape
        PolygonShape hitBoxShape = new PolygonShape();
        hitBoxShape.setAsBox(4, 12);

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
        batch.begin();
        TextureRegion frame = ((AnimatedEntity) entity).getFrame(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime), !GameBehavior.ATTACK.equals(getCurrentBehavior()));

        //Draw Will o wisp
        // While it WALKs, do not show. Is invisible! FIXME use spell
        // If not hurt or the flickering POI must be shown, draw the texture.
        if (!GameBehavior.WALK.equals(getCurrentBehavior()) && (!mustFlicker || !GameBehavior.HURT.equals(getCurrentBehavior()))) {
            //Draw shadow
            Vector2 drawPosition = adjustPosition();
            batch.draw(((WillowispEntity) entity).getShadowTexture(), drawPosition.x - POSITION_OFFSET, drawPosition.y - 2 - POSITION_Y_OFFSET);
            batch.draw(frame, drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);
        }

        // Every 1/8 seconds alternate between showing and hiding the texture to achieve flickering effect
        if (GameBehavior.HURT.equals(getCurrentBehavior()) && TimeUtils.timeSinceNanos(startToFlickTime) > GameScreen.FLICKER_DURATION_IN_NANO / 6) {
            mustFlicker = !mustFlicker;

            // restart flickering timer
            startToFlickTime = TimeUtils.nanoTime();
        }
        batch.end();
    }

    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {

    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        // End leech and cancel timer if present
    }

    /**
     * Method for hurting the FlyingCorpse
     *
     * @param attacker
     */
    @Override
    public void hurt(GameInstance attacker) {

        //50% chance of evading attack
        final boolean canEvade = (MathUtils.random(1, 100)) >= 50;
        if (isDying()) {
            ((WillowispEntity) entity).playDeathCry();
            body.setLinearVelocity(0, 0);
            changeCurrentBehavior(GameBehavior.DEAD);
        } else if (!canEvade && !GameBehavior.HURT.equals(getCurrentBehavior())) {
            ((WillowispEntity) entity).playHurtCry();

            // Hurt by player
            double amount = ((Damager) attacker).damageRoll();
            //If Undead or Otherworldly, halve normal lance damage
            if (((PlayerInstance) attacker).getItemQuantityFound(ItemEnum.HOLY_LANCE) < 2) {
                amount = Math.floor(amount / 2);
            }

            this.damage += Math.min(getResistance(), amount);
            changeCurrentBehavior(GameBehavior.HURT);
            Gdx.app.log(LoggerUtils.DEBUG_TAG, "Instance " + this.getClass().getSimpleName() + " total damage " + damage);
            postHurtLogic(attacker);
        } else if (canEvade && !GameBehavior.EVADE.equals(getCurrentBehavior())) {
            ((WillowispEntity) entity).playEvadeSwift();
            //Just evade
            changeCurrentBehavior(GameBehavior.EVADE);
            Gdx.app.log(LoggerUtils.DEBUG_TAG, "Instance EVADED!");
            postHurtLogic(attacker);
        }
    }

    @Override
    public void postHurtLogic(GameInstance attacker) {

        // is pushed away while flickering
        Vector2 direction = new Vector2(attacker.getBody().getPosition().x - body.getPosition().x,
                attacker.getBody().getPosition().y - body.getPosition().y).nor();

        float modifier = 4f;
        //If evading, the leap is more subtle and perpendicular
        if (GameBehavior.EVADE.equals(getCurrentBehavior())) {
            modifier = 1.5f;
            direction = direction.rotate90(MathUtils.randomBoolean() ? 0 :1 );
        }
        body.setLinearVelocity(WILLOWISP_SPEED * modifier * -direction.x, WILLOWISP_SPEED * modifier * -direction.y);
        // Do nothing for half second
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                changeCurrentBehavior(GameBehavior.IDLE);
            }
        }, 0.25f);
    }


    @Override
    public int getResistance() {
        return 9;
    }

    public double damageRoll() {
        return 2;
    }

    /**
     * Handle the attack logic, activating and deactivating attack collision bodies
     *
     * @param stateTime
     */
    private void attackLogic(float stateTime) {

        int currentFrame = ((AnimatedEntity) entity).getFrameIndex(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime));

        //Activate weapon sensor on frame
        if (currentFrame == ATTACK_VALID_FRAME) {
            startAttackCooldown = TimeUtils.nanoTime();
            switch (currentDirectionEnum) {
                case UP: {
                    upTentacleBody.setActive(true);
                    break;
                }
                case DOWN: {
                    downTentacleBody.setActive(true);
                    break;
                }
                case LEFT: {
                    leftTentacleBody.setActive(true);
                    break;
                }
                case RIGHT: {
                    rightTentacleBody.setActive(true);
                    break;
                }
            }
        } else {
            deactivateAttackBodies();
        }

        if (GameBehavior.ATTACK.equals(getCurrentBehavior()) && ((WillowispEntity) entity).isAnimationFinished(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime))) {
            changeCurrentBehavior(GameBehavior.WALK);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        rightTentacleBody.getFixtureList().forEach(f ->
                rightTentacleBody.destroyFixture(f));
        leftTentacleBody.getFixtureList().forEach(f ->
                leftTentacleBody.destroyFixture(f));
        upTentacleBody.getFixtureList().forEach(f ->
                upTentacleBody.destroyFixture(f));
        downTentacleBody.getFixtureList().forEach(f ->
                downTentacleBody.destroyFixture(f));
    }

    @Override
    public boolean isDisposable() {
        return isDead();
    }

    private float mapStateTimeFromBehaviour(float stateTime) {

        switch (getCurrentBehavior()) {
            case IDLE:
            case ATTACK: {
                return (stateTime - attackDeltaTime);
            }
            default: {
                return stateTime;
            }
        }
    }

    /**
     * Deactivate all attacker bodies
     */
    private void deactivateAttackBodies() {
        rightTentacleBody.setActive(false);
        upTentacleBody.setActive(false);
        leftTentacleBody.setActive(false);
        downTentacleBody.setActive(false);
    }

    @Override
    protected float getLineOfSight() {
        return 1000f;
    }
}
