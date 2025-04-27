package com.faust.lhengine.game.gameentities.enums;

/**
 * Game Behavior class.
 * This enum tells us which behaviour the GameInstance is.
 * Used for animation and ScriptActor steps
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum GameBehavior {
    WALK,
    ATTACK,
    HURT,
    KNEE,
    IDLE,
    DEAD,
    LAYING,
    EVADE,

    //These two GameBehavior are not used in real case scenarios, but only as steps for ScriptActorInstances
    UNDEFINED,
    STUB,
    MOCK;

    public static GameBehavior getFromOrdinal(int ord) {
        return GameBehavior.values()[ord];
    }
}
