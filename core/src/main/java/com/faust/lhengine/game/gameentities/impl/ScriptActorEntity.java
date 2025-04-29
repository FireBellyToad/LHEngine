package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.faust.lhengine.game.scripts.enums.ScriptCommandsEnum;
import com.faust.lhengine.game.scripts.enums.ScriptActorType;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;

import java.util.*;


/**
 * Echo Actor entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ScriptActorEntity extends AnimatedEntity {

    private static final int MAX_SUPPORTED_ROWS = 5; //for sprite sheet

    private final ScriptActorType scriptActorType;
    private Sound startingSound;
    private int precalculatedRows = MAX_SUPPORTED_ROWS;


    //Each step, ordered
    private final List<GameBehavior> stepOrder = new ArrayList<>();
    protected final Map<GameBehavior, Map<ScriptCommandsEnum, Object>> commands = new HashMap<>();

    public ScriptActorEntity(ScriptActorType scriptActorType, AssetManager assetManager) {
        super(assetManager.get(scriptActorType.getSpriteFilename()));
        this.scriptActorType = scriptActorType;
        if (Objects.nonNull(scriptActorType.getSoundFileName())) {
            this.startingSound = assetManager.get(scriptActorType.getSoundFileName());
        }

        loadEchoActorSteps();
    }

    /**
     * Initialize steps and seconds per step
     */
    private void loadEchoActorSteps() {

        JsonValue parsedSteps = new JsonReader().parse(Gdx.files.internal("scripts/" + scriptActorType.getFilename())).get("steps");

        //Precalculate rows from steps
        precalculatedRows = parsedSteps.size;

        //Commands map initialization and calculation of repeated animation rows.
        int stepCounter = 0;
        int reusedAnimationSteps = 0;
        for (JsonValue s : parsedSteps) {

            GameBehavior behaviour = GameBehavior.getFromOrdinal(stepCounter);
            Objects.requireNonNull(behaviour);
            stepOrder.add(behaviour);

            commands.put(behaviour, extractValue(new HashMap<>(), s.child));

            // Handle "useAnimationOfStep" and "repeatStep"
            if (commands.get(behaviour).containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
                reusedAnimationSteps++;
            } else  if (commands.get(behaviour).containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
                int stepToRepeat = (Integer) (commands.get(behaviour).get(ScriptCommandsEnum.REPEAT_STEP));
                if (commands.get(GameBehavior.getFromOrdinal(stepToRepeat)).containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
                    reusedAnimationSteps++;
                } 
            }

            stepCounter++;
        }
        //reduce rows with precalculated reusedAnimationSteps value
        precalculatedRows -= reusedAnimationSteps;

        //Extract all TextureRegion, given the new amout if rows were duplicated
        TextureRegion[] allFrames = getFramesFromTexture();

        //We need to reinitialize this because we must go from first row to last
        stepCounter = 0;
        //We need to reinitialize this because we can support duplicated rows in between EchoActor animations!
        reusedAnimationSteps = 0;
        //Bind each animation row to each step
        for (GameBehavior step : stepOrder) {
            //If this step doesn't reuse another step's animation, save it
            if (commands.get(step).containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
                reusedAnimationSteps++;
            } else  if (commands.get(step).containsKey(ScriptCommandsEnum.REPEAT_STEP)) {
                int stepToRepeat = (Integer) (commands.get(step).get(ScriptCommandsEnum.REPEAT_STEP));
                if (commands.get(GameBehavior.getFromOrdinal(stepToRepeat)).containsKey(ScriptCommandsEnum.USE_ANIMATION_OF_STEP)) {
                    reusedAnimationSteps++;
                } else {                
                    addAnimation(new Animation<>(FRAME_DURATION, Arrays.copyOfRange(allFrames, getTextureColumns() * (stepCounter - reusedAnimationSteps), getTextureColumns() * (stepCounter - reusedAnimationSteps + 1))), step);
                }
            } else {                
                addAnimation(new Animation<>(FRAME_DURATION, Arrays.copyOfRange(allFrames, getTextureColumns() * (stepCounter - reusedAnimationSteps), getTextureColumns() * (stepCounter - reusedAnimationSteps + 1))), step);
            }

            stepCounter++;
        }
    }

    /**
     * Extracts all values and put them in map
     *
     * @param values
     * @param jsonValue
     */
    private Map<ScriptCommandsEnum, Object> extractValue(final Map<ScriptCommandsEnum, Object> values, final JsonValue jsonValue) {

        if (Objects.nonNull(jsonValue)) {

            ScriptCommandsEnum extractedCommand = ScriptCommandsEnum.getFromCommandString(jsonValue.name());

            if (Objects.nonNull(extractedCommand)) {

                if (extractedCommand.getValueClass().equals(String.class)) {
                    values.put(extractedCommand, jsonValue.asString());
                } else if (extractedCommand.getValueClass().equals(Integer.class)) {
                    values.put(extractedCommand, jsonValue.asInt());
                } else if (extractedCommand.getValueClass().equals(Boolean.class)) {
                    values.put(extractedCommand, jsonValue.asBoolean());
                } else if (extractedCommand.getValueClass().equals(ScriptCommandsEnum.class)) {
                    extractValue(values, jsonValue.child);
                }

                // Go to next
                if (Objects.nonNull(jsonValue.next)) {
                    extractValue(values, jsonValue.next);
                }
            }
        }
        return values;
    }

    @Override
    protected void initAnimations() {
        //Nothing to do here... initializing animation after load
    }

    public List<GameBehavior> getStepOrder() {
        return stepOrder;
    }

    @Override
    protected int getTextureColumns() {
        return 6;
    }

    @Override
    protected int getTextureRows() {
        return precalculatedRows;
    }

    public ScriptActorType getEchoesActorType() {
        return scriptActorType;
    }

    /**
     * Get commands to do in given "step"
     *
     * @param step
     * @return
     */
    public Map<ScriptCommandsEnum, Object> getCommandsForStep(GameBehavior step) {
        return commands.getOrDefault(step, null);
    }

    /**
     * Play starting sound if valid
     */
    public void playStartingSound() {
        if (Objects.nonNull(startingSound)) {
            startingSound.play(0.75f);
        }
    }

    public void stopStartingSound() {
        if (Objects.nonNull(startingSound)) {
            startingSound.stop();
        }
    }

}
