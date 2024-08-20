package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.faust.lhengine.game.rooms.AbstractRoom;
import com.faust.lhengine.game.rooms.OnRoomChangeListener;
import com.faust.lhengine.game.scripts.enums.ScriptActorType;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.gameentities.enums.PlayerFlag;
import com.faust.lhengine.game.gameentities.impl.PlayerEntity;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Hurtable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.rooms.areas.TriggerArea;
import com.faust.lhengine.game.world.manager.CollisionManager;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.utils.GameInstanceUtils;
import com.faust.lhengine.utils.LoggerUtils;
import com.faust.lhengine.utils.ShaderWrapper;

import java.util.*;

/**
 * Player Instance class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class PlayerInstance extends AnimatedInstance implements InputProcessor, Hurtable, Damager, OnRoomChangeListener {

    private static final float PLAYER_SPEED = 50;
    private static final float PLAYER_SPEED_SUBMERGED = 40;
    private static final int EXAMINATION_DISTANCE = 25;
    private static final int ATTACK_VALID_FRAME = 6; // Frame to activate attack sensor
    private static final float SPEAR_SENSOR_Y_OFFSET = 8;
    private static final long HEALTH_KIT_TIME_IN_MILLIS = 4000;
    private static final int MAX_AVAILABLE_HEALTH_KIT = 3;
    private static final long CONFUSION_TIME_IN_MILLIS = 3000;
    private static final long POST_HURT_COOLDOWN_DURATION = 500;

    // Time delta between state and start of attack animation
    private float attackDeltaTime = 0;

    //Body for spear attacks
    private Body downSpearBody;
    private Body leftSpearBody;
    private Body rightSpearBody;
    private Body upSpearBody;

    private final List<POIInstance> roomPoiList = new ArrayList<>();
    private POIInstance nearestPOIInstance;

    private int availableHealthKits = 0; // available Health Kits
    private long startHealingTime;
    private long confusionTimeout = 0;
    private long postHurtCooldown = 0;
    private final Map<ItemEnum, Integer> itemsFound = new HashMap<> ();
    private final Map<PlayerFlag, Boolean> playerFlags = new HashMap<>();
    private TriggerArea triggerToActivate;

    private boolean muteSounds = false;
    private boolean canStillTurnWhileAttacking = true;


    public PlayerInstance(AssetManager assetManager, boolean isWebBuild) {
        super(new PlayerEntity(assetManager, isWebBuild));


        currentDirectionEnum = DirectionEnum.DOWN;

        Gdx.input.setInputProcessor(this);
        ((PlayerEntity) entity).getWaterWalkEffect().start();

        for(PlayerFlag flag : PlayerFlag.values()){
            playerFlags.put(flag,false);
        }
    }

    @Override
    public int getResistance() {
        //6 without armor, 8 with armor
        return 6 + ((getItemQuantityFound(ItemEnum.ARMOR) > 0) ? 2 : 0);
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        translateAccessoryBodies();
        ((PlayerEntity) entity).getWaterWalkEffect().setPosition(body.getPosition().x, body.getPosition().y);

        //if is Dead, play death animation and then game over
        if (isDead()) {
            muteSounds = true;
            setPlayerLinearVelocity(0, 0);
            leftSpearBody.setActive(false);
            upSpearBody.setActive(false);
            downSpearBody.setActive(false);
            rightSpearBody.setActive(false);
            //Init delta time
            if (!GameBehavior.DEAD.equals(getCurrentBehavior())) {
                changeCurrentBehavior(GameBehavior.DEAD);
                attackDeltaTime = stateTime;
                ((PlayerEntity) entity).playDeathCry();
            } else if (((PlayerEntity) entity).isAnimationFinished(getCurrentBehavior(), mapStateTimeFromBehaviour(stateTime))) {
                playerFlags.put(PlayerFlag.GO_TO_GAMEOVER,true);
            }
            //Do not do anything else
            return;
        }

        //In endgame do nothing
        if (playerFlags.get(PlayerFlag.PREPARE_END_GAME)) {
            changeCurrentBehavior(GameBehavior.IDLE);
            setPlayerLinearVelocity(0, 0);
            return;
        }

        //If hurt, deactivate hitbox and don't do anything
        hitBox.setActive(!GameBehavior.HURT.equals(getCurrentBehavior()));
        if (GameBehavior.HURT.equals(getCurrentBehavior()))
            return;

        // Interrupt healing if moving
        if (GameBehavior.KNEE.equals(getCurrentBehavior())) {
            if (TimeUtils.timeSinceNanos(startHealingTime) >= TimeUtils.millisToNanos(HEALTH_KIT_TIME_IN_MILLIS)) {
                availableHealthKits--;
                startHealingTime = 0;
                damage = 0;
                changeCurrentBehavior(GameBehavior.IDLE);
            }
            if (this.body.getLinearVelocity().x != 0 ||
                    this.body.getLinearVelocity().y != 0) {
                changeCurrentBehavior(GameBehavior.WALK);
            }
        }

        // If not healing
        if (!GameBehavior.KNEE.equals(getCurrentBehavior())) {
            if (startHealingTime > 0) {
                // Resetting healing timer if healing is interrupted by anything
                startHealingTime = 0;
                changeCurrentBehavior(GameBehavior.IDLE);
            }

            if (GameBehavior.ATTACK.equals(getCurrentBehavior())) {
                // If attacking do attack logic
                setPlayerLinearVelocity(0, 0);
                attackLogic(stateTime);
            } else {
                deactivateAttackBodies();

                // If the player has stopped moving, set idle behaviour
                if (this.body.getLinearVelocity().x == 0 && this.body.getLinearVelocity().y == 0) {
                    changeCurrentBehavior(GameBehavior.IDLE);
                } else {
                    changeCurrentBehavior(GameBehavior.WALK);
                }

                // Set horizontal direction if horizontal velocity is not zero
                if (this.body.getLinearVelocity().x == PLAYER_SPEED || this.body.getLinearVelocity().x == PLAYER_SPEED_SUBMERGED) {
                    this.currentDirectionEnum = DirectionEnum.RIGHT;
                } else if (this.body.getLinearVelocity().x == -PLAYER_SPEED || this.body.getLinearVelocity().x == -PLAYER_SPEED_SUBMERGED) {
                    this.currentDirectionEnum = DirectionEnum.LEFT;
                }

                // Set vertical direction if vertical velocity is not zero
                if (this.body.getLinearVelocity().y == PLAYER_SPEED || this.body.getLinearVelocity().y == PLAYER_SPEED_SUBMERGED) {
                    this.currentDirectionEnum = DirectionEnum.UP;
                } else if (this.body.getLinearVelocity().y == -PLAYER_SPEED || this.body.getLinearVelocity().y == -PLAYER_SPEED_SUBMERGED) {
                    this.currentDirectionEnum = DirectionEnum.DOWN;
                }

            }
        }

        // Checking if there is any unexamined POI near enough to be examined by the player
        if (!roomPoiList.isEmpty()) {

            roomPoiList.forEach((poi) -> poi.setEnableFlicker(false));

            nearestPOIInstance =  GameInstanceUtils.getNearestInstance(this,roomPoiList);

            if (nearestPOIInstance.getBody().getPosition().dst(getBody().getPosition()) <= EXAMINATION_DISTANCE &&
                    !nearestPOIInstance.isAlreadyExamined()) {
                nearestPOIInstance.setEnableFlicker(true);
            }
        }

        //If is confused, check if must start countdown or end confusion
        if (playerFlags.get(PlayerFlag.IS_CONFUSED)) {
            if (confusionTimeout == 0) {
                confusionTimeout = TimeUtils.nanoTime();
            } else if (TimeUtils.timeSinceNanos(confusionTimeout) >= TimeUtils.millisToNanos(CONFUSION_TIME_IN_MILLIS)) {
                //Stop confusion after 3 seconds
                confusionTimeout = 0;
                playerFlags.put(PlayerFlag.IS_CONFUSED,false);
            }
        }
    }

    /**
     * Translate all accessory body
     */
    private void translateAccessoryBodies() {
        rightSpearBody.setTransform(body.getPosition().x + 12, body.getPosition().y + SPEAR_SENSOR_Y_OFFSET, 0);
        upSpearBody.setTransform(body.getPosition().x - 4, body.getPosition().y + 12 + SPEAR_SENSOR_Y_OFFSET, 0);
        leftSpearBody.setTransform(body.getPosition().x - 12, body.getPosition().y + SPEAR_SENSOR_Y_OFFSET, 0);
        downSpearBody.setTransform(body.getPosition().x - 4, body.getPosition().y - 12 + SPEAR_SENSOR_Y_OFFSET, 0);
        hitBox.setTransform(body.getPosition().x, body.getPosition().y + 8, 0);
    }

    @Override
    public void hurt(GameInstance attacker) {

        //If not in post hurt invincibility, take damage or die
        if (TimeUtils.timeSinceNanos(postHurtCooldown) > TimeUtils.millisToNanos(POST_HURT_COOLDOWN_DURATION)) {
            double damageReceived = ((Damager) attacker).damageRoll();
            if (damageReceived > 0 && isDying()) {
                playerFlags.put(PlayerFlag.IS_DEAD,true);
            } else {
                postHurtCooldown = TimeUtils.nanoTime();
                ((PlayerEntity) entity).playHurtCry();
                this.damage += Math.min(getResistance(), damageReceived);
                Gdx.app.log(LoggerUtils.DEBUG_TAG, "Instance " + this.getClass().getSimpleName() + " total damage " + damage);
                postHurtLogic(attacker);
            }
        }
    }

    @Override
    public void postHurtLogic(GameInstance attacker) {
        // is pushed away while flickering if not attacked by Strix
        Vector2 direction = new Vector2(attacker.getBody().getPosition().x - body.getPosition().x,
                attacker.getBody().getPosition().y - body.getPosition().y).nor();

        //Strix, Hurtspell and Infernum must not send player back on hurt
        if (!(attacker instanceof MonsterBirdInstance) && !(attacker instanceof HurtingSpellInstance) &&
                !(attacker instanceof ScriptActorInstance && (ScriptActorType.INFERNUM.equals(((ScriptActorInstance) attacker).getType())))) {
            body.setLinearVelocity(PLAYER_SPEED * 2 * -direction.x, PLAYER_SPEED * 2 * -direction.y);
        }
        changeCurrentBehavior(GameBehavior.HURT);
        // Do nothing for half second
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                changeCurrentBehavior(GameBehavior.IDLE);
                body.setLinearVelocity(0, 0);
            }
        }, 0.20f);
    }

    /**
     * Triggers the examination event of the nearest POI found
     */
    private void examineNearestPOI() {
        Objects.requireNonNull(nearestPOIInstance);
        if (nearestPOIInstance.getBody().getPosition().dst(getBody().getPosition()) <= EXAMINATION_DISTANCE &&
                !nearestPOIInstance.isAlreadyExamined()) {
            nearestPOIInstance.examine(this);
        }
    }

    /**
     * Changed the POI list that the Player Instance must check
     *
     * @param poiNewList
     */
    public void changePOIList(List<POIInstance> poiNewList) {
        Objects.requireNonNull(poiNewList);
        roomPoiList.clear();
        roomPoiList.addAll(poiNewList);
        if (!roomPoiList.isEmpty()) {
            nearestPOIInstance = GameInstanceUtils.getNearestInstance(this,roomPoiList);
        }
    }

    /**
     * Handles player bodies movement
     *
     * @param horizontalVelocity
     * @param verticalVelocity
     */
    private void setPlayerLinearVelocity(float horizontalVelocity, float verticalVelocity) {

        this.body.setLinearVelocity(horizontalVelocity, verticalVelocity);
    }

    public void stopAll() {
        this.changeCurrentBehavior(GameBehavior.IDLE);
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

        int xOffset = 0;
        int yOffset = 0;

        if (GameBehavior.ATTACK.equals(getCurrentBehavior())) {
            switch (currentDirectionEnum) {
                case LEFT: {
                    xOffset = 1;
                    break;
                }
                case RIGHT: {
                    xOffset = -1;
                    yOffset = 1;
                    break;
                }
                case UP: {
                    yOffset = -3;
                    break;
                }
            }
        }


        //Draw Walfrit
        // If not hurt or the flickering POI must be shown, draw the texture
        if (!mustFlicker || !GameBehavior.HURT.equals(getCurrentBehavior())) {

            drawWalfritShaded(batch, stateTime, xOffset, yOffset);
        }

        // Every 1/8 seconds alternate between showing and hiding the texture to achieve flickering effect
        if (GameBehavior.HURT.equals(getCurrentBehavior()) && TimeUtils.timeSinceNanos(startToFlickTime) > GameScreen.FLICKER_DURATION_IN_NANO / 6) {
            mustFlicker = !mustFlicker;

            // restart flickering timer
            startToFlickTime = TimeUtils.nanoTime();
        }
        batch.end();

    }

    /**
     * Draw Walfrit sprites handling shading
     *
     * @param batch
     * @param stateTime
     * @param xOffset
     * @param yOffset
     */
    private void drawWalfritShaded(SpriteBatch batch, float stateTime, int xOffset, int yOffset) {

        // Get frame (looping if is not dead)
        TextureRegion frame = ((PlayerEntity) entity).getFrame(getCurrentBehavior(), currentDirectionEnum,
                mapStateTimeFromBehaviour(stateTime), !GameBehavior.DEAD.equals(getCurrentBehavior()));

        //Get shader and set parameters
        ShaderWrapper shader = ((PlayerEntity) entity).getPlayerShader();
        shader.addFlag("hasArmor", itemsFound.getOrDefault(ItemEnum.ARMOR, 0) == 1);
        shader.addFlag("hasHolyLance", itemsFound.getOrDefault(ItemEnum.HOLY_LANCE, 0) == 2);
        shader.addFlag("isConfused", playerFlags.get(PlayerFlag.IS_CONFUSED) ? 1 : 0);

        shader.setShaderOnBatchWithFlags(batch);

        Vector2 drawPosition = adjustPosition();
        //Draw shadow
        batch.draw(((PlayerEntity) entity).getShadowTexture(), drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);

        final ParticleEffect waterWalkEffect = ((PlayerEntity) entity).getWaterWalkEffect();

        //Draw watersteps if submerged
        if (playerFlags.get(PlayerFlag.IS_SUBMERGED)) {
            waterWalkEffect.draw(batch, Gdx.graphics.getDeltaTime());
            yOffset += 2;
            // Do not loop if is not doing anything
            if (waterWalkEffect.isComplete() && GameBehavior.WALK.equals(getCurrentBehavior())) {
                waterWalkEffect.reset();
            }
        } else {
            waterWalkEffect.reset();
        }

        batch.draw(frame, drawPosition.x - xOffset - POSITION_OFFSET, drawPosition.y - yOffset - POSITION_Y_OFFSET);

        //Restore default shader
        shader.resetDefaultShader(batch);
    }

    /**
     * Alter state time for different animation speed based on current behaviour
     *
     * @param stateTime
     * @return
     */
    private float mapStateTimeFromBehaviour(float stateTime) {
        switch (getCurrentBehavior()) {
            case DEAD: {
                return stateTime - attackDeltaTime;
            }
            case ATTACK: {
                return 2.25f * (stateTime - attackDeltaTime);
            }
            default: {
                return stateTime;
            }
        }
    }

    /**
     * Handle the attack logic, activating and deactivating attack collision bodies
     *
     * @param stateTime
     */
    private void attackLogic(float stateTime) {

        if (attackDeltaTime == 0)
            attackDeltaTime = stateTime;

        //Activate spear bodies when in right frame
        final int currentFrame = ((AnimatedEntity) entity).getFrameIndex(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime));
        if (currentFrame >= ATTACK_VALID_FRAME && currentFrame < ATTACK_VALID_FRAME + 4) {
            canStillTurnWhileAttacking = false;
            switch (currentDirectionEnum) {
                case UP: {
                    upSpearBody.setActive(true);
                    break;
                }
                case DOWN: {
                    downSpearBody.setActive(true);
                    break;
                }
                case LEFT: {
                    leftSpearBody.setActive(true);
                    break;
                }
                case RIGHT: {
                    rightSpearBody.setActive(true);
                    break;
                }
            }
        } else {
            canStillTurnWhileAttacking = true;
            deactivateAttackBodies();
        }

        // Resetting Behaviour on animation end
        if (((AnimatedEntity) entity).isAnimationFinished(getCurrentBehavior(), currentDirectionEnum, mapStateTimeFromBehaviour(stateTime))) {
            changeCurrentBehavior(GameBehavior.IDLE);
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
        shape.setAsBox(4, 2);

        // Define Fixtures
        FixtureDef mainFixtureDef = new FixtureDef();
        mainFixtureDef.shape = shape;
        mainFixtureDef.density = 1;
        mainFixtureDef.friction = 1;
        mainFixtureDef.filter.categoryBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);

        body.setUserData(this);
        body.createFixture(mainFixtureDef);
        shape.dispose();

        BodyDef rightSpearDef = new BodyDef();
        rightSpearDef.type = BodyDef.BodyType.KinematicBody;
        rightSpearDef.fixedRotation = true;
        rightSpearDef.position.set(x + 2, y-1);

        // Define shape
        PolygonShape rightSpearShape = new PolygonShape();
        rightSpearShape.setAsBox(6, 2);

        // Define Fixtures
        FixtureDef rightSpearFixtureDef = new FixtureDef();
        rightSpearFixtureDef.shape = rightSpearShape;
        rightSpearFixtureDef.density = 1;
        rightSpearFixtureDef.friction = 1;
        rightSpearFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        rightSpearFixtureDef.filter.maskBits = CollisionManager.ENEMY_GROUP;

        // Associate body to world
        rightSpearBody = world.createBody(rightSpearDef);
        rightSpearBody.setUserData(this);
        rightSpearBody.createFixture(rightSpearFixtureDef);
        rightSpearBody.setActive(false);
        rightSpearShape.dispose();

        BodyDef upSpearDef = new BodyDef();
        upSpearDef.type = BodyDef.BodyType.KinematicBody;
        upSpearDef.fixedRotation = true;
        upSpearDef.position.set(x, y - 1);

        // Define shape
        PolygonShape upSpearShape = new PolygonShape();
        upSpearShape.setAsBox(2, 6);

        // Define Fixtures
        FixtureDef upSpearFixtureDef = new FixtureDef();
        upSpearFixtureDef.shape = upSpearShape;
        upSpearFixtureDef.density = 1;
        upSpearFixtureDef.friction = 1;
        upSpearFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        upSpearFixtureDef.filter.maskBits = CollisionManager.ENEMY_GROUP;

        // Associate body to world
        upSpearBody = world.createBody(upSpearDef);
        upSpearBody.setUserData(this);
        upSpearBody.createFixture(upSpearFixtureDef);
        upSpearBody.setActive(false);
        upSpearShape.dispose();

        BodyDef leftSpearDef = new BodyDef();
        leftSpearDef.type = BodyDef.BodyType.KinematicBody;
        leftSpearDef.fixedRotation = true;
        leftSpearDef.position.set(x - 2, y);

        // Define shape
        PolygonShape leftSpearShape = new PolygonShape();
        leftSpearShape.setAsBox(6, 2);

        // Define Fixtures
        FixtureDef leftSpearFixtureDef = new FixtureDef();
        leftSpearFixtureDef.shape = leftSpearShape;
        leftSpearFixtureDef.density = 1;
        leftSpearFixtureDef.friction = 1;
        leftSpearFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        leftSpearFixtureDef.filter.maskBits = CollisionManager.ENEMY_GROUP;

        // Associate body to world
        leftSpearBody = world.createBody(leftSpearDef);
        leftSpearBody.setUserData(this);
        leftSpearBody.createFixture(leftSpearFixtureDef);
        leftSpearBody.setActive(false);
        leftSpearShape.dispose();

        BodyDef downSpearDef = new BodyDef();
        downSpearDef.type = BodyDef.BodyType.KinematicBody;
        downSpearDef.fixedRotation = true;
        downSpearDef.position.set(x, y +1);

        // Define shape
        PolygonShape downSpearShape = new PolygonShape();
        downSpearShape.setAsBox(2, 6);

        // Define Fixtures
        FixtureDef downSpearFixtureDef = new FixtureDef();
        downSpearFixtureDef.shape = downSpearShape;
        downSpearFixtureDef.density = 1;
        downSpearFixtureDef.friction = 1;
        downSpearFixtureDef.filter.categoryBits = CollisionManager.WEAPON_GROUP;
        downSpearFixtureDef.filter.maskBits = CollisionManager.ENEMY_GROUP;

        // Associate body to world
        downSpearBody = world.createBody(downSpearDef);
        downSpearBody.setUserData(this);
        downSpearBody.createFixture(downSpearFixtureDef);
        downSpearBody.setActive(false);
        downSpearShape.dispose();

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
        hitBoxFixtureDef.filter.categoryBits = CollisionManager.PLAYER_GROUP;
        hitBoxFixtureDef.filter.maskBits = CollisionManager.WEAPON_GROUP;

        // Associate body to world
        hitBox = world.createBody(hitBoxDef);
        hitBox.setUserData(this);
        hitBox.createFixture(hitBoxFixtureDef);
        hitBoxShape.dispose();
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    //----------------------------------------------- CONTROLS -----------------------------------------------

    @Override
    public boolean keyDown(int keycode) {

        if (playerFlags.get(PlayerFlag.PREPARE_END_GAME)) {
            setPlayerLinearVelocity(0, 0);
            return false;
        }

        // If hurt o dying, can't do anything
        if (GameBehavior.HURT.equals(getCurrentBehavior()) || GameBehavior.DEAD.equals(getCurrentBehavior())) {
            return false;
        }

        // Keep the initial velocity
        float horizontalVelocity = this.body.getLinearVelocity().x;
        float verticalVelocity = this.body.getLinearVelocity().y;

        // Set instance direction and velocity accordingly to the pressed key
        // Check if not moving in opposite direction.
        // If attacking, can turn just before doing the final thrust with the spear.
        // This is done to smooth out the attack while turning and soften up a little the controls
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP: {
                if (!GameBehavior.ATTACK.equals(getCurrentBehavior()) && verticalVelocity != -PLAYER_SPEED) {
                    verticalVelocity = playerFlags.get(PlayerFlag.IS_SUBMERGED) ? PLAYER_SPEED_SUBMERGED : PLAYER_SPEED;
                } else if (GameBehavior.ATTACK.equals(getCurrentBehavior()) && canStillTurnWhileAttacking) {
                    this.currentDirectionEnum = DirectionEnum.UP;
                }
                break;
            }
            case Input.Keys.S:
            case Input.Keys.DOWN: {
                if (!GameBehavior.ATTACK.equals(getCurrentBehavior()) && verticalVelocity != PLAYER_SPEED) {
                    verticalVelocity = playerFlags.get(PlayerFlag.IS_SUBMERGED) ? -PLAYER_SPEED_SUBMERGED : -PLAYER_SPEED;
                } else if (GameBehavior.ATTACK.equals(getCurrentBehavior()) && canStillTurnWhileAttacking) {
                    this.currentDirectionEnum = DirectionEnum.DOWN;
                }
                break;
            }
            case Input.Keys.A:
            case Input.Keys.LEFT: {
                if (!GameBehavior.ATTACK.equals(getCurrentBehavior()) && horizontalVelocity != PLAYER_SPEED) {
                    horizontalVelocity = playerFlags.get(PlayerFlag.IS_SUBMERGED) ? -PLAYER_SPEED_SUBMERGED : -PLAYER_SPEED;
                } else if (GameBehavior.ATTACK.equals(getCurrentBehavior()) && canStillTurnWhileAttacking) {
                    this.currentDirectionEnum = DirectionEnum.LEFT;
                }
                break;
            }
            case Input.Keys.D:
            case Input.Keys.RIGHT: {
                if (!GameBehavior.ATTACK.equals(getCurrentBehavior()) && horizontalVelocity != -PLAYER_SPEED) {
                    horizontalVelocity = playerFlags.get(PlayerFlag.IS_SUBMERGED) ? PLAYER_SPEED_SUBMERGED : PLAYER_SPEED;
                } else if (GameBehavior.ATTACK.equals(getCurrentBehavior()) && canStillTurnWhileAttacking) {
                    this.currentDirectionEnum = DirectionEnum.RIGHT;
                }
                break;
            }
            case Input.Keys.X:
            case Input.Keys.K: {
                if (Objects.nonNull(nearestPOIInstance)) {
                    examineNearestPOI();
                    horizontalVelocity = 0;
                    verticalVelocity = 0;

                    //If nearest POI is referenced by a Echo trigger, activate that echo
                    if(Objects.nonNull(triggerToActivate) && nearestPOIInstance.equals(triggerToActivate.getReferencedInstance())){
                        triggerToActivate.activate(this);
                    }
                }
                break;
            }
            case Input.Keys.Z:
            case Input.Keys.J: {
                //Attacks
                if (!GameBehavior.ATTACK.equals(getCurrentBehavior())) {
                    ((PlayerEntity) entity).playLanceSwing();
                    changeCurrentBehavior(GameBehavior.ATTACK);
                    attackDeltaTime = 0;
                }
                break;
            }
            case Input.Keys.C:
            case Input.Keys.L: {
                //Tries to heal himself
                this.useHealthKit();
                break;
            }
            case Input.Keys.ESCAPE:
            case Input.Keys.P: {
                //Pause game
                playerFlags.put(PlayerFlag.PAUSE_GAME, true);
                break;
            }
        }

        //If is confused, invert commands
        if (playerFlags.get(PlayerFlag.IS_CONFUSED)) {
            horizontalVelocity = -horizontalVelocity;
            verticalVelocity = -verticalVelocity;
        }

        setPlayerLinearVelocity(horizontalVelocity, verticalVelocity);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {

        // Do not handle anything if is in endgame
        if (playerFlags.get(PlayerFlag.PREPARE_END_GAME)) {
            return false;
        }

        // If hurt, can't do anything
        if (GameBehavior.HURT.equals(getCurrentBehavior())) {
            return false;
        }
        // Keep the initial velocity
        float horizontalVelocity = this.body.getLinearVelocity().x;
        float verticalVelocity = this.body.getLinearVelocity().y;

        // Determine new velocity
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.S:
            case Input.Keys.UP:
            case Input.Keys.DOWN: {
                verticalVelocity = 0;
                break;
            }
            case Input.Keys.A:
            case Input.Keys.D:
            case Input.Keys.LEFT:
            case Input.Keys.RIGHT: {
                horizontalVelocity = 0;
                break;
            }
            case Input.Keys.X:
            case Input.Keys.K: {
                break;
            }
        }
        setPlayerLinearVelocity(horizontalVelocity, verticalVelocity);
        return true;
    }

    /**
     * Walfrit cures himself with a health kit, instancing a isHealingTimer
     */
    private void useHealthKit() {
        //If has at least one healing kit, is not already curing himself and at least 1 damage point
        if (startHealingTime == 0 && availableHealthKits > 0 && damage > 0) {
            //Cures himself if not interrupted
            changeCurrentBehavior(GameBehavior.KNEE);
            startHealingTime = TimeUtils.nanoTime();
        }
    }

    /**
     * Called whenever the player finds an item
     *
     * @param itemFound
     */
    public void onItemFound(ItemEnum itemFound) {

        ((PlayerEntity) entity).playBonusSound();

        //Set item to 1 if not found before, or else add 1
        itemsFound.merge(itemFound, 1, Integer::sum);

        if (itemFound == ItemEnum.HEALTH_KIT) {//Increase available Kits, max 3
            availableHealthKits = Math.min(MAX_AVAILABLE_HEALTH_KIT, availableHealthKits + 1);
        } else {
            Gdx.app.log("WARN", "No special implementation for item " + itemFound.name());
        }
    }

    /**
     * @return current available Health kits
     */
    public int getAvailableHealthKits() {
        return availableHealthKits;
    }

    public void setAvailableHealthKits(int availableHealthKits) {
        this.availableHealthKits = availableHealthKits;
    }

    /**
     * @param itemFound
     * @return quantity found of the item
     */
    public int getItemQuantityFound(ItemEnum itemFound) {
        Objects.requireNonNull(itemFound);
        return itemsFound.getOrDefault(itemFound, 0);
    }

    /**
     * Set the item quantity (usually from savefile)
     *
     * @param item
     * @param quantity
     */
    public void setItemQuantityFound(ItemEnum item, int quantity) {
        Objects.requireNonNull(item);
        itemsFound.put(item, quantity);
    }

    /**
     * @return healing timer instance, non null if the player is curing himself
     */
    public long getStartHealingTime() {
        return startHealingTime;
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
        return playerFlags.get(PlayerFlag.IS_DEAD);
    }


    public int getDamageDelta() {
        return getResistance() - damage;
    }

    public void cleanReferences() {
        nearestPOIInstance = null;
        //Reset particle emitter and change position
        ((PlayerEntity) entity).getWaterWalkEffect().setPosition(startX, startY);
        ((PlayerEntity) entity).getWaterWalkEffect().reset();
    }

    public void setSubmerged(boolean submerged) {
        Vector2 velocity = this.body.getLinearVelocity();
        if (!muteSounds && submerged && !playerFlags.get(PlayerFlag.IS_SUBMERGED)) {
            //Play sound when Walfrit gets in water
            ((PlayerEntity) entity).playWaterSplash();

            //Restore water speed
            velocity.x = (velocity.x / PLAYER_SPEED) * PLAYER_SPEED_SUBMERGED;
            velocity.y = (velocity.y / PLAYER_SPEED) * PLAYER_SPEED_SUBMERGED;
        } else if (!submerged) {

            //Restore terrain speed
            velocity.x = (velocity.x / PLAYER_SPEED_SUBMERGED) * PLAYER_SPEED;
            velocity.y = (velocity.y / PLAYER_SPEED_SUBMERGED) * PLAYER_SPEED;
        }

        //Clamp speed if over the maximum one
        setPlayerLinearVelocity(MathUtils.clamp(velocity.x, -PLAYER_SPEED, PLAYER_SPEED),
                MathUtils.clamp(velocity.y, -PLAYER_SPEED, PLAYER_SPEED));

        playerFlags.put(PlayerFlag.IS_SUBMERGED, submerged);
    }

    public double damageRoll() {
        return 2 + MathUtils.random(1, 4);
    }

    @Override
    public void dispose() {
        super.dispose();
        rightSpearBody.getFixtureList().forEach(f ->
                rightSpearBody.destroyFixture(f));
        upSpearBody.getFixtureList().forEach(f ->
                upSpearBody.destroyFixture(f));
        leftSpearBody.getFixtureList().forEach(f ->
                leftSpearBody.destroyFixture(f));
        downSpearBody.getFixtureList().forEach(f ->
                downSpearBody.destroyFixture(f));
        ((PlayerEntity) entity).getWaterWalkEffect().dispose();

    }

    @Override
    //Player is never disposable
    public boolean isDisposable() {
        return false;
    }

    /**
     * Deactivate all attacker bodies
     */
    private void deactivateAttackBodies() {
        rightSpearBody.setActive(false);
        upSpearBody.setActive(false);
        leftSpearBody.setActive(false);
        downSpearBody.setActive(false);
    }

    /**
     *
     * @param flagToGet
     */
    public void setPlayerFlagValue(PlayerFlag flagToGet, boolean value){
        Objects.requireNonNull(flagToGet);

        playerFlags.put(flagToGet, value);
    }

    /**
     *
     * @param flagToGet
     * @return flag value
     */
    public boolean getPlayerFlagValue(PlayerFlag flagToGet){
        Objects.requireNonNull(flagToGet);

        return playerFlags.get(flagToGet);
    }

    /**
     * Reset this PlayerInstance as inputprocessor
     */
    public void setAsInputProcessor() {
        playerFlags.put(PlayerFlag.PAUSE_GAME,false);
        changeCurrentBehavior(GameBehavior.IDLE);
        setPlayerLinearVelocity(0, 0);
        Gdx.input.setInputProcessor(this);
    }

    public void setTriggerToActivate(TriggerArea triggerToActivate) {
        this.triggerToActivate = triggerToActivate;
    }

    @Override
    public void onRoomChangeStart(AbstractRoom newRoom) {
        //Sounds are not played on roomchange
        muteSounds = true;
    }

    @Override
    public void onRoomChangeEnd(AbstractRoom newRoom) {
        muteSounds = false;
        //In roomchange, idle and do nothing
        changeCurrentBehavior(GameBehavior.IDLE);
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
