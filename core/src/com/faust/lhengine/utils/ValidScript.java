package com.faust.lhengine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.faust.lhengine.game.scripts.enums.ScriptCommandsEnum;
import com.faust.lhengine.game.scripts.enums.ScriptActorType;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.gameentities.enums.EnemyEnum;
import com.faust.lhengine.game.gameentities.enums.POIEnum;
import com.faust.lhengine.game.instances.interfaces.Killable;
import com.faust.lhengine.game.rooms.enums.MapLayersEnum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Valid Script extractedCommands enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ValidScript {

    /**
     * Validate a set of steps
     *
     * @param parsedSteps steps to validate
     * @param filename    filename of the extracted step (readonly, used for logs)
     * @throws ScriptValidationException
     */
    @SuppressWarnings("unchecked")
    public static void validate(JsonValue parsedSteps, String filename) throws ScriptValidationException {
        int parsedStepNumber = 0;
        final Set<ScriptCommandsEnum> incompatibleCommandInStepSet = new HashSet();
        try {
            Objects.requireNonNull(parsedSteps);

            for (JsonValue s : parsedSteps) {
                inspect(s.child, filename, parsedStepNumber, incompatibleCommandInStepSet);
                parsedStepNumber++;
                incompatibleCommandInStepSet.clear();
            }
        } catch (ScriptValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptValidationException(e.getMessage());
        }

    }

    /**
     * Validate a single step
     *
     * @param child                        step to validate
     * @param filename                     filename of the extracted step (readonly, used for logs)
     * @param parsedStepNumber             (readonly, used for logs)
     * @param incompatibleCommandInStepSet
     * @throws ScriptValidationException
     */
    private static void inspect(JsonValue child, final String filename, final int parsedStepNumber, final Set<ScriptCommandsEnum> incompatibleCommandInStepSet) throws ScriptValidationException {

        try {
            if (Objects.nonNull(child)) {

                ScriptCommandsEnum extractedCommand = ScriptCommandsEnum.getFromCommandString(child.name());

                //parent structure validation
                if (Objects.nonNull(child.parent)) {
                    ScriptCommandsEnum extractedParent = ScriptCommandsEnum.getFromCommandString(child.parent.name());
                    if (Objects.nonNull(extractedParent)) {
                        if (Objects.nonNull(extractedParent.getSubCommands()) && Arrays.stream(extractedParent.getSubCommands()).noneMatch(extractedCommand::equals)) {
                            throw new IllegalArgumentException(extractedCommand.getCommandString() + " is not a child of " + extractedParent.getCommandString());
                        }
                    }
                }

                //get value
                Object extractedValue = null;
                if (Objects.nonNull(extractedCommand)) {

                    //Valid extractedValue check
                    if (extractedCommand.getValueClass().equals(String.class)) {
                        extractedValue = child.asString();
                    } else if (extractedCommand.getValueClass().equals(Integer.class)) {
                        extractedValue = child.asInt();
                    } else if (extractedCommand.getValueClass().equals(Boolean.class)) {
                        extractedValue = child.asBoolean();
                    } else if (extractedCommand.getValueClass().equals(ScriptCommandsEnum.class)) {

                        //is required check on subcommands
                        for (ScriptCommandsEnum subCommand : extractedCommand.getSubCommands()) {
                            if (subCommand.isRequired() && !child.has(subCommand.getCommandString())) {
                                throw new NullPointerException(subCommand.getCommandString() + " is required!");
                            }
                        }

                        //Recursion for children extractedCommands
                        inspect(child.child, filename, parsedStepNumber, incompatibleCommandInStepSet);
                    }
                }
                EnemyEnum enemyEnum = null;

                //String values validation
                assert extractedValue instanceof String;

                switch (extractedCommand) {
                    case IF_AT_LEAST_ONE_KILLABLE_ALIVE: {

                        try {
                            enemyEnum = EnemyEnum.valueOf((String) extractedValue);
                        } catch (Exception e) {
                            throw new IllegalArgumentException(extractedValue + " is not valid Killable!");
                        }

                        if (!ClassReflection.isAssignableFrom(Killable.class, enemyEnum.getInstanceClass())) {
                            throw new IllegalArgumentException(extractedValue + " is not valid Killable!");
                        }
                        break;
                    }
                    case DIRECTION: {
                        DirectionEnum.valueOf((String) extractedValue);
                        break;
                    }
                    case RENDER_ONLY_MAP_LAYER: {
                        MapLayersEnum.valueOf((String) extractedValue);
                        break;
                    }
                    case IF_AT_LEAST_ONE_POI_EXAMINABLE: {
                        POIEnum.valueOf((String) extractedValue);
                        break;
                    }
                    case IDENTIFIER: {

                        POIEnum poiEnum = null;

                        try {
                            enemyEnum = EnemyEnum.valueOf((String) extractedValue);
                        } catch (Exception e) {
                            //Nothing to do here...
                        }
                        try {
                            poiEnum = POIEnum.valueOf((String) extractedValue);
                        } catch (Exception e) {
                            //Nothing to do here...
                        }
                        if (Objects.isNull(enemyEnum) && Objects.isNull(poiEnum)) {
                            throw new IllegalArgumentException(extractedValue + " is not valid POI or Enemy!");
                        }
                        break;
                    }
                    case USE_ANIMATION_OF_STEP: {
                        assert extractedValue != null;
                        if (extractedValue.equals(parsedStepNumber)) {
                            throw new IllegalArgumentException(" useAnimationOfStep cannot have value of " + extractedValue + " because is the same as the step!");
                        }
                        break;
                    }
                }

                checkIncompatibleCommands(extractedCommand, incompatibleCommandInStepSet);

                if (Objects.nonNull(child.next)) {
                    //Recursion for children extractedCommands
                    inspect(child.next, filename, parsedStepNumber, incompatibleCommandInStepSet);
                }
            }
        } catch (ScriptValidationException e) {
            //Just throws it
            throw e;
        } catch (Exception e) {
            //Prepare nice output!
            String stepName = (Objects.isNull(child.parent) || Objects.isNull(child.parent.name)) ? child.name : child.parent.name + " -> " + child.name;
            String exceptionClassName = e.getClass().getSimpleName();
            throw new ScriptValidationException(filename + " step " + parsedStepNumber + " -> extractedCommand " + stepName + " : " + exceptionClassName + " " + e.getMessage());
        }
    }

    /**
     * @param extractedCommand
     * @param incompatibleCommandInStepSet
     */
    private static void checkIncompatibleCommands(ScriptCommandsEnum extractedCommand, Set<ScriptCommandsEnum> incompatibleCommandInStepSet) {
        switch (extractedCommand) {
            case STEP:
            case END: {
                if (!incompatibleCommandInStepSet.contains(ScriptCommandsEnum.STEP) && !incompatibleCommandInStepSet.contains(ScriptCommandsEnum.END)) {
                    incompatibleCommandInStepSet.add(extractedCommand);
                } else {
                    throw new IllegalArgumentException("\"step\" command and \"end\" command cannot be on same step!");
                }
            }
            default:
                break;

        }
    }

    /**
     * Validate all game scripts
     *
     * @throws ScriptValidationException
     */
    public static void validateAllScriptsGdx() throws ScriptValidationException {

        for (ScriptActorType scriptActorType : ScriptActorType.values()) {
            // start validate
            JsonValue parsedSteps = new JsonReader().parse(Gdx.files.internal("scripts/" + scriptActorType.getFilename())).get("steps");
            validate(parsedSteps, scriptActorType.getFilename());
        }
    }
}