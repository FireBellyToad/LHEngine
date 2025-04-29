package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.EnemyEnum;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.enums.POIEnum;
import com.faust.lhengine.game.gameentities.impl.ScriptActorEntity;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.instances.interfaces.Damager;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.interfaces.SpawnFactory;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.rooms.areas.TriggerArea;
import com.faust.lhengine.game.rooms.enums.MapLayersEnum;
import com.faust.lhengine.game.scripts.enums.ScriptActorType;
import com.faust.lhengine.game.scripts.enums.ScriptCommandsEnum;
import com.faust.lhengine.game.world.manager.CollisionManager;
import com.faust.lhengine.utils.LoggerUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for Echo Actors Instances
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ScriptActorInstance extends AnimatedInstance implements Interactable, Damager {

    private final TriggerArea triggerForActor;
    private boolean removeFromRoom = false;
    private boolean showTextBox = true;
    private float deltaTime = 0; // Time delta between step start and current
    private boolean echoIsActive;
    private final SpawnFactory spawnFactory;
    private int instanceCounter;

    public ScriptActorInstance(ScriptActorType scriptActorType, float x, float y, AssetManager assetManager,
            TriggerArea triggerForActor, SpawnFactory spawnFactory) {
        super(new ScriptActorEntity(scriptActorType, assetManager));
        this.startX = x;
        this.startY = y;
        this.echoIsActive = false;
        this.spawnFactory = spawnFactory;
        this.triggerForActor = triggerForActor;
        this.instanceCounter = 0;

        // get first step
        changeCurrentBehavior(((ScriptActorEntity) this.entity).getStepOrder().get(0));
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {

        // If must be removed, avoid logic
        if (!removeFromRoom) {

            // Check if echo is active. On first iteration set to true
            if (!echoIsActive) {
                echoIsActive = true;
            }

            // initialize deltatime
            if (deltaTime == 0)
                deltaTime = stateTime;

            // All commands to do in this step
            executeCommands(roomContent, stateTime);
        }
    }

    /**
     * Executes all the commands in one step
     *
     * @param roomContent the room contents
     * @param stateTime   stateTime of the main loop
     */
    private void executeCommands(RoomContent roomContent, float stateTime) {

        Map<ScriptCommandsEnum,Object> commands = ((ScriptActorEntity) this.entity).getCommandsForStep(getCurrentBehavior());

        if (commands.containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
            int stepToRepeat = (Integer) commands.get(ScriptCommandsEnum.REPEAT_STEP);
            commands = ((ScriptActorEntity) this.entity).getCommandsForStep(GameBehavior.getFromOrdinal(stepToRepeat));
        }

        final Boolean checkOnEveryFrame = (Boolean) commands.getOrDefault(ScriptCommandsEnum.CHECK_ON_EVERY_FRAME, false);

        //Move if should
        if (commands.containsKey(ScriptCommandsEnum.DIRECTION) && commands.containsKey(ScriptCommandsEnum.SPEED)) {
            DirectionEnum directionEnum = DirectionEnum.valueOf((String) commands.get(ScriptCommandsEnum.DIRECTION));
            float speed = (Integer) commands.get(ScriptCommandsEnum.SPEED);
            body.setLinearVelocity(getVelocityOfStep(directionEnum, speed));
        } else {
            body.setLinearVelocity(0, 0);
        }

        //Reuse animation of another step if specified
        GameBehavior animationToUse = getCurrentBehavior();
        if (commands.containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
            animationToUse = ((ScriptActorEntity) entity).getStepOrder().get((Integer) commands.get(ScriptCommandsEnum.USE_ANIMATION_OF_STEP));
        }

        // Increase counter if needed
        if (commands.containsKey(ScriptCommandsEnum.INCREASE_COUNTER_OF) ) {
            this.instanceCounter += (Integer) commands.get(ScriptCommandsEnum.INCREASE_COUNTER_OF);
        }

        //If checkOnEveryFrame is true, that potentially cut the time to do check and go to next step
        Integer index = null;
        boolean isJustNextStep = true;
        if (checkOnEveryFrame) {
            final List<GameBehavior> stepOrder = ((ScriptActorEntity) entity).getStepOrder();
            index = getNewIndex(stepOrder, commands, roomContent);
            isJustNextStep = index == stepOrder.indexOf(getCurrentBehavior());
        }

        //If a new step index is found AND is not just next step OR animation is ended THEN pass to the next step
        if ((index != null && !isJustNextStep) || ((ScriptActorEntity) this.entity).isAnimationFinished(animationToUse, mapStateTimeFromBehaviour(stateTime))) {
            final List<GameBehavior> stepOrder = ((ScriptActorEntity) entity).getStepOrder();
            if (index == null) {
                index = getNewIndex(stepOrder, commands, roomContent);
            }

            Gdx.app.log(LoggerUtils.DEBUG_TAG, "Echo Actor " + ((ScriptActorEntity) entity).getEchoesActorType() + " end step " + getCurrentBehavior());

            deltaTime = stateTime;
            //If is not last step
            if (index + 1 < stepOrder.size()) {
                //Before stepping out, hurt player and check if canKill player
                if (commands.containsKey(ScriptCommandsEnum.DAMAGE) &&
                        ((Boolean) commands.getOrDefault(ScriptCommandsEnum.CAN_KILL_PLAYER, true) || !roomContent.player.isDying())) {
                    roomContent.player.hurt(this);
                }

                //Go to next step
                changeCurrentBehavior(((ScriptActorEntity) entity).getStepOrder().get(index + 1));
                showTextBox = true;
            } else {
                removeFromRoom = true;
                spawnInstances(commands);
                Gdx.app.log(LoggerUtils.DEBUG_TAG, "Echo Actor " + ((ScriptActorEntity) entity).getEchoesActorType() + " must be removed ");
            }
        }
    }

    /**
     * New step from logics
     *
     * @param stepOrder
     * @param commands
     * @param roomContent
     * @return
     */
    private int getNewIndex(List<GameBehavior> stepOrder, Map<ScriptCommandsEnum, Object> commands,
            RoomContent roomContent) {

        // If has "go to step" or "terminate", handle it correctly
        if (commands.containsKey(ScriptCommandsEnum.STEP)) {

            final Integer index = (Integer) commands.get(ScriptCommandsEnum.STEP);

            if (checkConditionalCommands(commands, roomContent)) {

                // If no "until" condition, just jump to "go to step" value
                return stepOrder.indexOf(GameBehavior.getFromOrdinal(index));

            }

        } else if (commands.containsKey(ScriptCommandsEnum.END)) {
            // return Size to end echo
            if (checkConditionalCommands(commands, roomContent)) {
                return stepOrder.size();
            }
        }

        // or else, just get next
        return stepOrder.indexOf(getCurrentBehavior());
    }

    /**
     * @param commands
     * @param roomContent
     * @return true if one of the conditionals commands is true
     */
    private boolean checkConditionalCommands(Map<ScriptCommandsEnum, Object> commands, RoomContent roomContent) {

        // If there are no conditions, just go
        if (!commands.containsKey(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_LESS_THAN)
                && !commands.containsKey(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_MORE_THAN) &&
                !commands.containsKey(ScriptCommandsEnum.IF_AT_LEAST_ONE_KILLABLE_ALIVE)
                && !commands.containsKey(ScriptCommandsEnum.IF_NO_KILLABLE_ALIVE) &&
                !commands.containsKey(ScriptCommandsEnum.IF_NO_KILLABLE_ALIVE)
                && !commands.containsKey(ScriptCommandsEnum.IF_AT_LEAST_ONE_POI_EXAMINABLE) &&
                !commands.containsKey(ScriptCommandsEnum.IF_COUNTER_IS_GREATER_THAN)
                && !commands.containsKey(ScriptCommandsEnum.IF_COUNTER_IS_LESS_THAN)) {
            return true;
        }

        // If onlyOneConditionMustBeTrue = true, we use OR instead of AND when checking
        // conditions.
        // That means that the initial value of this variable must be respectively
        // "true" or "false" to
        // ensure checking is done right
        final boolean onlyOneConditionMustBeTrue = (boolean) commands
                .getOrDefault(ScriptCommandsEnum.ONLY_ONE_CONDITION_MUST_BE_TRUE, false);
        boolean areConditionsTrue = !onlyOneConditionMustBeTrue;

        // Check condition on until Player has at least less then N damage (priority on
        // other checks)
        if (commands.containsKey(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_LESS_THAN)) {
            // Extract value of damage
            final int value = (int) commands.get(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_LESS_THAN);
            areConditionsTrue = roomContent.player.getDamage() <= value;
        } else if (commands.containsKey(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_MORE_THAN)) {
            // Check condition on if Player has more then N damage (priority on other
            // checks)
            final int value = (int) commands.get(ScriptCommandsEnum.IF_PLAYER_DAMAGE_IS_MORE_THAN);
            areConditionsTrue = roomContent.player.getDamage() > value;
        }

        // Check condition on until there is at least one enemy of type is alive in room
        if (commands.containsKey(ScriptCommandsEnum.IF_AT_LEAST_ONE_KILLABLE_ALIVE)) {
            // Extract instance class from enum and do check
            final EnemyEnum enemyEnum = EnemyEnum
                    .valueOf((String) commands.get(ScriptCommandsEnum.IF_AT_LEAST_ONE_KILLABLE_ALIVE));
            final Class<? extends AnimatedInstance> enemyClass = enemyEnum.getInstanceClass();

            if (onlyOneConditionMustBeTrue) {
                areConditionsTrue = areConditionsTrue || roomContent.enemyList.stream()
                        .anyMatch(e -> enemyClass.equals(e.getClass()) && !((Killable) e).isDead());
            } else {
                areConditionsTrue = areConditionsTrue && roomContent.enemyList.stream()
                        .anyMatch(e -> enemyClass.equals(e.getClass()) && !((Killable) e).isDead());
            }

        } else if (commands.containsKey(ScriptCommandsEnum.IF_NO_KILLABLE_ALIVE)) {
            // Check condition on if there is no enemy of type is alive in room
            // Extract instance class from enum and do check
            final EnemyEnum enemyEnum = EnemyEnum
                    .valueOf((String) commands.get(ScriptCommandsEnum.IF_NO_KILLABLE_ALIVE));
            final Class<? extends AnimatedInstance> enemyClass = enemyEnum.getInstanceClass();

            if (onlyOneConditionMustBeTrue) {
                areConditionsTrue = areConditionsTrue || roomContent.enemyList.stream()
                        .noneMatch(e -> enemyClass.equals(e.getClass()) && !((Killable) e).isDead());
            } else {
                areConditionsTrue = areConditionsTrue && roomContent.enemyList.stream()
                        .noneMatch(e -> enemyClass.equals(e.getClass()) && !((Killable) e).isDead());
            }

        }

        // Check condition on until there is in the room at least one examinable POI
        if (commands.containsKey(ScriptCommandsEnum.IF_AT_LEAST_ONE_POI_EXAMINABLE)) {
            // Extract Poi type and do check
            final POIEnum poiEnum = POIEnum
                    .valueOf((String) commands.get(ScriptCommandsEnum.IF_AT_LEAST_ONE_POI_EXAMINABLE));

            if (onlyOneConditionMustBeTrue) {
                areConditionsTrue = areConditionsTrue || roomContent.poiList.stream()
                        .anyMatch(poi -> poiEnum.equals(poi.getType()) && poi.isAlreadyExamined());
            } else {
                areConditionsTrue = areConditionsTrue && roomContent.poiList.stream()
                        .anyMatch(poi -> poiEnum.equals(poi.getType()) && poi.isAlreadyExamined());
            }
        }

        // Check condition on counter
        if (commands.containsKey(ScriptCommandsEnum.IF_COUNTER_IS_GREATER_THAN)) {
            // Extract instance class from enum and do check
            final int counterValue = (Integer) commands.get(ScriptCommandsEnum.IF_COUNTER_IS_GREATER_THAN);

            if (onlyOneConditionMustBeTrue) {
                areConditionsTrue = areConditionsTrue || (this.instanceCounter > counterValue);
            } else {
                areConditionsTrue = areConditionsTrue && (this.instanceCounter > counterValue);
            }

        } else if (commands.containsKey(ScriptCommandsEnum.IF_COUNTER_IS_LESS_THAN)) {
            // Extract instance class from enum and do check
            final int counterValue = (Integer) commands.get(ScriptCommandsEnum.IF_COUNTER_IS_LESS_THAN);

            if (onlyOneConditionMustBeTrue) {
                areConditionsTrue = areConditionsTrue || (this.instanceCounter < counterValue);
            } else {
                areConditionsTrue = areConditionsTrue && (this.instanceCounter < counterValue);
            }

        }

        return areConditionsTrue;
    }

    /**
     * Spawn instance if doable
     *
     * @param commands
     */
    private void spawnInstances(Map<ScriptCommandsEnum, Object> commands) {

        // Should not spawn anything if has no identifier
        if (!commands.containsKey(ScriptCommandsEnum.IDENTIFIER))
            return;

        // FIXME use class name?
        String thingName = (String) commands.get(ScriptCommandsEnum.IDENTIFIER);
        EnemyEnum enemyEnum = null;
        POIEnum poiEnum = null;

        try {
            enemyEnum = EnemyEnum.valueOf(thingName);
        } catch (Exception e) {
            // Nothing to do here...
        }

        try {
            poiEnum = POIEnum.valueOf(thingName);
        } catch (Exception e) {
            // Nothing to do here...
        }

        // Set spawn coordinates
        float spawnX = startX;
        float spawnY = startY;
        boolean useRelative = (boolean) commands.getOrDefault(ScriptCommandsEnum.RELATIVE, false);

        if (commands.containsKey(ScriptCommandsEnum.X)) {
            final int value = (int) commands.get(ScriptCommandsEnum.X);
            spawnX = useRelative ? spawnX + value : value;
        }
        if (commands.containsKey(ScriptCommandsEnum.Y)) {
            final int value = (int) commands.get(ScriptCommandsEnum.Y);
            spawnY = useRelative ? spawnY + value : value;
        }

        if (Objects.nonNull(enemyEnum)) {
            spawnFactory.spawnInstance(enemyEnum.getInstanceClass(), spawnX, spawnY, enemyEnum.name());
        } else if (Objects.nonNull(poiEnum)) {
            spawnFactory.spawnInstance(POIInstance.class, spawnX, spawnY, poiEnum.name());
        }
    }

    /**
     * @param direction
     * @param speedInCurrentStep
     * @return velocity of movement to do in Vector2 format
     */
    private Vector2 getVelocityOfStep(DirectionEnum direction, float speedInCurrentStep) {
        switch (direction) {
            case UP:
                return new Vector2(0, speedInCurrentStep);
            case DOWN:
                return new Vector2(0, -speedInCurrentStep);
            case RIGHT:
                return new Vector2(speedInCurrentStep, 0);
            case LEFT:
                return new Vector2(-speedInCurrentStep, 0);
        }
        return null;
    }

    private float mapStateTimeFromBehaviour(float stateTime) {
        switch (((ScriptActorEntity) entity).getEchoesActorType()) {
            case DEAD_HAND:
            case DEAD_DOUBLE_HAND:
                // Glitchy movement for skeletons
                return 1.5f * (stateTime - deltaTime);

            default:
                return 0.75f * (stateTime - deltaTime);
        }
    }

    @Override
    public void createBody(World world, float x, float y) {
        Objects.requireNonNull(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(x, y);

        // Define shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8, 6);

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;
        fixtureDef.friction = 1;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CollisionManager.SOLID_GROUP;
        fixtureDef.filter.maskBits = CollisionManager.PLAYER_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        shape.dispose();
    }

    @Override
    public void draw(SpriteBatch batch, float stateTime) {
        Objects.requireNonNull(batch);

        // Do not draw if must be removed
        if (removeFromRoom) {
            ((ScriptActorEntity) entity).stopStartingSound();
            return;
        }

        Map<ScriptCommandsEnum, Object> commands = ((ScriptActorEntity) this.entity)
                .getCommandsForStep(getCurrentBehavior());

        if (commands.containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
            int stepToRepeat = (Integer) commands.get(ScriptCommandsEnum.REPEAT_STEP);
            commands = ((ScriptActorEntity) this.entity).getCommandsForStep(GameBehavior.getFromOrdinal(stepToRepeat));
        }

        // If has ScriptCommandsEnum.INVISIBLE == true don't draw anything
        if ((Boolean) commands.getOrDefault(ScriptCommandsEnum.INVISIBLE, false)) {
            return;
        }

        batch.begin();

        // Reuse animation of another step if specified
        GameBehavior animationToUse = getCurrentBehavior();
        if (commands.containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
            animationToUse = ((ScriptActorEntity) entity).getStepOrder()
                    .get((Integer) commands.get(ScriptCommandsEnum.USE_ANIMATION_OF_STEP));
        }

        // Should not loop!
        TextureRegion frame = ((AnimatedEntity) entity).getFrame(animationToUse, mapStateTimeFromBehaviour(stateTime));

        Vector2 drawPosition = adjustPosition();
        batch.draw(frame, drawPosition.x - POSITION_OFFSET, drawPosition.y - POSITION_Y_OFFSET);
        batch.end();
    }

    public boolean mustRemoveFromRoom() {
        return removeFromRoom;
    }

    /**
     * @return TextBox key to show based on current actor step
     */
    public String getCurrentTextBoxToShow() {
        if (showTextBox) {
            showTextBox = false;
            Map<ScriptCommandsEnum, Object> commandOnCurrentStep = ((ScriptActorEntity) entity)
                    .getCommandsForStep(getCurrentBehavior());

            if (commandOnCurrentStep.containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
                int stepToRepeat = (Integer) commandOnCurrentStep.get(ScriptCommandsEnum.REPEAT_STEP);
                commandOnCurrentStep = ((ScriptActorEntity) this.entity).getCommandsForStep(GameBehavior.getFromOrdinal(stepToRepeat));
            }
                    
            return (String) commandOnCurrentStep.get(ScriptCommandsEnum.TEXTBOX_KEY);
        }

        return null;
    }

    public boolean hasCurrentTextBoxToShow() {
        return ((ScriptActorEntity) entity).getCommandsForStep(getCurrentBehavior())
                .containsKey(ScriptCommandsEnum.TEXTBOX_KEY) && showTextBox;
    }

    public void playStartingSound() {
        ((ScriptActorEntity) entity).playStartingSound();
    }

    @Override
    public double damageRoll() {
        // Only certain echoes should harm the player
        switch (((ScriptActorEntity) entity).getEchoesActorType()) {
            case DEAD_HAND:
            case DEAD_DOUBLE_HAND:
                // Ld6
                return Math.min(MathUtils.random(1, 6), MathUtils.random(1, 6));
            case HORROR:
            case INFERNUM:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {
        // If active, hurt player
        if (echoIsActive) {
            playerInstance.hurt(this);
        }
    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        // Nothing to do here... yet
    }

    /**
     * @return the Map layer to draw
     */
    public String overrideMapLayerDrawn() {
        Map<ScriptCommandsEnum, Object> extractedCommand = ((ScriptActorEntity) this.entity)
                .getCommandsForStep(getCurrentBehavior());

        if (extractedCommand.containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
            int stepToRepeat = (Integer) extractedCommand.get(ScriptCommandsEnum.REPEAT_STEP);
            extractedCommand = ((ScriptActorEntity) this.entity).getCommandsForStep(GameBehavior.getFromOrdinal(stepToRepeat));
        }
                
        final String layerString = (String) extractedCommand.get(ScriptCommandsEnum.RENDER_ONLY_MAP_LAYER);
        return Objects.isNull(layerString) ? MapLayersEnum.TERRAIN_LAYER.getLayerName()
                : MapLayersEnum.valueOf(layerString).getLayerName();
    }

    public ScriptActorType getType() {
        return ((ScriptActorEntity) entity).getEchoesActorType();
    }

    public TriggerArea getTriggerForActor() {
        return triggerForActor;
    }

    public boolean isEchoIsActive() {
        return echoIsActive;
    }
}
