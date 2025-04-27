package com.faust.lhengine.game.scripts.enums;

/**
 * Valid Echo commands enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum ScriptCommandsEnum {
    //Simple
    TEXTBOX_KEY("textBoxKey", String.class),
    RENDER_ONLY_MAP_LAYER("renderOnlyMapLayer", String.class),
    DIRECTION("direction", String.class, true),
    SPEED("speed", Integer.class, true),
    STEP("step", Integer.class),
    IF_AT_LEAST_ONE_KILLABLE_ALIVE("ifAtLeastOneKillableAlive", String.class),
    IF_AT_LEAST_ONE_POI_EXAMINABLE("ifAtLeastOnePOIExaminable", String.class),
    IF_PLAYER_DAMAGE_IS_LESS_THAN("ifPlayerDamageIsLessThan", Integer.class),
    IF_COUNTER_IS_GREATER_THAN("ifCounterIsGreaterThan", Integer.class),
    IF_COUNTER_IS_LESS_THAN("ifCounterIsLessThan", Integer.class),
    SPLASH_TO_SHOW("splashToShow", String.class),
    INSTANCE_CLASS("instanceClass", String.class, true),
    IDENTIFIER("identifier", String.class, true),
    X("x", Integer.class),
    Y("y", Integer.class),
    RELATIVE("relative", Boolean.class),
    INVISIBLE("invisible", Boolean.class),
    DAMAGE("damage", Integer.class, true),
    CAN_KILL_PLAYER("canKillPlayer", Boolean.class),
    IF_NO_KILLABLE_ALIVE("ifNoKillableAlive", String.class),
    IF_PLAYER_DAMAGE_IS_MORE_THAN("ifPlayerDamageIsMoreThan", Integer.class),
    USE_ANIMATION_OF_STEP("useAnimationOfStep", Integer.class),
    CHECK_ON_EVERY_FRAME("checkOnEveryFrame", Boolean.class),
    END("end", Boolean.class),
    ONLY_ONE_CONDITION_MUST_BE_TRUE("onlyOneConditionMustBeTrue", Boolean.class),
    INCREASE_COUNTER_OF("increaseCounterOf", Integer.class),

    //Composite
    COUNTER("hurtPlayer", ScriptCommandsEnum.class, new ScriptCommandsEnum[]{DAMAGE, CAN_KILL_PLAYER}),
    HURT_PLAYER("hurtPlayer", ScriptCommandsEnum.class, new ScriptCommandsEnum[]{DAMAGE, CAN_KILL_PLAYER}),
    MOVE("move", ScriptCommandsEnum.class, new ScriptCommandsEnum[]{DIRECTION, SPEED}),
    GO_TO("goTo", ScriptCommandsEnum.class, new ScriptCommandsEnum[]{STEP, END, ONLY_ONE_CONDITION_MUST_BE_TRUE, IF_NO_KILLABLE_ALIVE, IF_PLAYER_DAMAGE_IS_MORE_THAN, IF_AT_LEAST_ONE_KILLABLE_ALIVE, IF_AT_LEAST_ONE_POI_EXAMINABLE, IF_PLAYER_DAMAGE_IS_LESS_THAN, CHECK_ON_EVERY_FRAME,IF_COUNTER_IS_GREATER_THAN,IF_COUNTER_IS_LESS_THAN}),
    SPAWN("spawn", ScriptCommandsEnum.class, new ScriptCommandsEnum[]{IDENTIFIER, X, Y, RELATIVE});

    private final String commandString;
    private final Class<?> valueClass;
    private final ScriptCommandsEnum[] subCommands;
    private final boolean isRequired;


    ScriptCommandsEnum(String commandString, Class<?> valueClass) {
        this.commandString = commandString;
        this.valueClass = valueClass;
        this.isRequired = false;
        this.subCommands = null;
    }

    ScriptCommandsEnum(String commandString, Class<?> valueClass, boolean isRequired) {
        this.commandString = commandString;
        this.valueClass = valueClass;
        this.isRequired = isRequired;
        this.subCommands = null;
    }

    ScriptCommandsEnum(String commandString, Class<ScriptCommandsEnum> valueClass, ScriptCommandsEnum[] subCommands) {
        this.commandString = commandString;
        this.valueClass = valueClass;
        this.isRequired = false;
        this.subCommands = subCommands;
    }

    public String getCommandString() {
        return commandString;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public ScriptCommandsEnum[] getSubCommands() {
        return subCommands;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public static ScriptCommandsEnum getFromCommandString(String command) {
        for (ScriptCommandsEnum e : ScriptCommandsEnum.values()) {
            if (e.getCommandString().equals(command)) {
                return e;
            }
        }
        return null;
    }
}

