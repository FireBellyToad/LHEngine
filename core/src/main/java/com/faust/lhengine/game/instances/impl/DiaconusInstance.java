package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.gameentities.enums.PlayerFlag;
import com.faust.lhengine.game.gameentities.impl.DiaconusEntity;
import com.faust.lhengine.game.instances.DistancerInstance;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.rooms.interfaces.SpawnFactory;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Hurtable;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.game.music.enums.TuneEnum;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.manager.CollisionManager;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.Objects;

/**
 * Diaconus secret boss instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class DiaconusInstance extends DistancerInstance implements Interactable, Hurtable, Damager {

    private static final float DIACONUS_SPEED = 40;
    private static final int LINE_OF_ATTACK = 40;
    private static final int ATTACK_VALID_FRAME = 3; // Frame to activate attack sensor
    private static final long ATTACK_COOLDOWN_TIME = 750; // in millis

    private final SpawnFactory spawnFactory;
    private final MusicManager musicManager;

    // Time delta between state and start of attack animation
    private float attackDeltaTime = 0;
    private boolean isSubmerged = false;

    private long startAttackCooldown = 0;

    public DiaconusInstance(float x, float y, PlayerInstance target, AssetManager assetManager, MusicManager musicManager, SpawnFactory spawnFactory) {
        super(new DiaconusEntity(assetManager), target);
        this.spawnFactory = spawnFactory;
        currentDirectionEnum = DirectionEnum.DOWN;
        this.startX = x;
        this.startY = y;
        ((DiaconusEntity) entity).getWaterWalkEffect().start();

        //Change Music
        this.musicManager = musicManager;

    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        //Move hitbox with main body
        hitBox.setTransform(body.getPosition().x, body.getPosition().y + 8, 0);

        //Move emitter
        ((DiaconusEntity) entity).getWaterWalkEffect().setPosition(body.getPosition().x, body.getPosition().y);


        //Change Music
        if (!musicManager.isPlaying() && !((PlayerInstance )target).isDead()) {
            this.musicManager.stopMusic();
            this.musicManager.playMusic(TuneEnum.SECRET, 0.45f);
        }


        if (GameBehavior.EVADE.equals(getCurrentBehavior()) || GameBehavior.HURT.equals(getCurrentBehavior()) || GameBehavior.DEAD.equals(getCurrentBehavior()))
            return;

        if (target.getBody().getPosition().dst(getBody().getPosition()) > 70  && !GameBehavior.ATTACK.equals(getCurrentBehavior())) {
            changeCurrentBehavior(GameBehavior.WALK);
            calculateNewGoal(roomContent.roomGraph);

            // Normal from Diaconus position to target
            final Vector2 destination = target.getBody().getPosition();
            Vector2 direction = new Vector2(destination.x - body.getPosition().x,
                    destination.y - body.getPosition().y).nor();

            currentDirectionEnum = extractDirectionFromNormal(direction);

            // Move towards target
            body.setLinearVelocity(DIACONUS_SPEED * direction.x, DIACONUS_SPEED * direction.y);
        } else if (!((Killable)target).isDead() && TimeUtils.timeSinceNanos(startAttackCooldown) > TimeUtils.millisToNanos(ATTACK_COOLDOWN_TIME) && (GameBehavior.ATTACK.equals(getCurrentBehavior()) ||
                    target.getBody().getPosition().dst(getBody().getPosition()) > LINE_OF_ATTACK)) {

            //Try to attack if not dead. If is attacking and is too near, still needs to end the attack before escaping
            //from the player
            //Start animation
            if (!GameBehavior.ATTACK.equals(getCurrentBehavior())) {
                attackDeltaTime = stateTime;
                changeCurrentBehavior(GameBehavior.ATTACK);
            }

            // Normal from Diaconus position to target
            Vector2 direction = new Vector2(target.getBody().getPosition().x - body.getPosition().x,
                    target.getBody().getPosition().y - body.getPosition().y).nor();
            currentDirectionEnum = extractDirectionFromNormal(direction);

            attackLogic(stateTime);
            body.setLinearVelocity(0, 0);

        } else if (target.getBody().getPosition().dst(getBody().getPosition()) < LINE_OF_ATTACK  && !GameBehavior.ATTACK.equals(getCurrentBehavior())) {
            //FIXME add check that if still attacking keeps attacking
            changeCurrentBehavior(GameBehavior.WALK);
            calculateNewGoal(roomContent.roomGraph);

            // Normal from Diaconus position to target
            final Vector2 destination = getMovementDestination();
            Vector2 direction = new Vector2(destination.x - body.getPosition().x,
                    destination.y - body.getPosition().y).nor();

            currentDirectionEnum = extractDirectionFromNormal(direction);

            // Move towards target
            body.setLinearVelocity(DIACONUS_SPEED * direction.x, DIACONUS_SPEED * direction.y);
        } else {
            changeCurrentBehavior(GameBehavior.IDLE);

            body.setLinearVelocity(0, 0);
        }

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

        int yOffset = 0;
        final ParticleEffect waterWalkEffect = ((DiaconusEntity) entity).getWaterWalkEffect();

        //Draw watersteps if submerged
        if (isSubmerged) {
            waterWalkEffect.draw(batch, Gdx.graphics.getDeltaTime());
            yOffset += 2;
            // Do not loop if is not doing anything
            if (waterWalkEffect.isComplete() && GameBehavior.WALK.equals(getCurrentBehavior())) {
                waterWalkEffect.reset();
            }
        } else {
            waterWalkEffect.reset();
        }

        Vector2 drawPosition = adjustPosition();
        //Draw shadow
        batch.draw(((DiaconusEntity) entity).getShadowTexture(), drawPosition.x - POSITION_OFFSET, drawPosition.y - yOffset - 2 - POSITION_Y_OFFSET);

        //Draw Diaconus
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
     * Method for hurting the Diaconus
     *
     * @param attacker
     */
    @Override
    public void hurt(GameInstance attacker) {

        //50% chance of evading attack
        final boolean canEvade = (MathUtils.random(1, 100)) >= 50;
        if (isDying()) {
            ((DiaconusEntity) entity).playDeathCry();
            this.musicManager.stopMusic();
            this.body.setLinearVelocity(0, 0);
            changeCurrentBehavior(GameBehavior.DEAD);
            ((PlayerInstance) attacker).setPlayerFlagValue(PlayerFlag.HAS_KILLED_SECRET_BOSS, true);
        } else if (!canEvade && !GameBehavior.HURT.equals(getCurrentBehavior())) {
            ((DiaconusEntity) entity).playHurtCry();

            // Hurt by player
            double amount = ((Damager) attacker).damageRoll();
            //Diaconus reduces normal lance damage
            if (((PlayerInstance) attacker).getItemQuantityFound(ItemEnum.HOLY_LANCE) < 2) {
                amount = Math.floor(amount / 2);
            }

            this.damage += Math.min(getResistance(), amount);
            changeCurrentBehavior(GameBehavior.HURT);
            Gdx.app.log(LoggerUtils.DEBUG_TAG, "Instance " + this.getClass().getSimpleName() + " total damage " + damage);
            postHurtLogic(attacker);
        } else if (canEvade && !GameBehavior.EVADE.equals(getCurrentBehavior())) {
            ((DiaconusEntity) entity).playEvadeSwift();
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
            modifier = 2f;
            direction = direction.rotate90(MathUtils.randomBoolean() ? 0 :1 );
        }
        body.setLinearVelocity(DIACONUS_SPEED * modifier * -direction.x, DIACONUS_SPEED * modifier * -direction.y);
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
        return 7;
    }

    public double damageRoll() {
        return 3;
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

            if (MathUtils.random(1, 5) > 4) {
                ((DiaconusEntity) entity).playConfusionSpellSound();
                spawnFactory.spawnInstance(ConfusionSpellInstance.class,
                        this.body.getPosition().x,
                        this.body.getPosition().y, null);

            } else {
                ((DiaconusEntity) entity).playHurtSpellSound();
                spawnFactory.spawnInstance(HurtingSpellInstance.class,
                        this.body.getPosition().x,
                        this.body.getPosition().y, null);

            }

        }
    }

    @Override
    public boolean isDisposable() {
        return isDead();
    }

    private float mapStateTimeFromBehaviour(float stateTime) {

        if (getCurrentBehavior() == GameBehavior.ATTACK) {
            return (stateTime - attackDeltaTime);
        }
        return stateTime;
    }

    public void setSubmerged(boolean submerged) {
        isSubmerged = submerged;
    }
}
