package com.faust.lhengine.game.gameentities.enums;

/**
 * Player flags Enum
 *
 * This flags are used for all the boolean values that affects the player character and can be changed by the environment
 * or other GameInstances.
 *
 * If a boolean must changed by the PlayerInstance only, use a private boolean in PlayerInstance class instead.
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum PlayerFlag {
    IS_SUBMERGED,
    IS_DEAD,
    PREPARE_END_GAME,
    GO_TO_GAMEOVER,
    PAUSE_GAME,
    HAS_KILLED_SECRET_BOSS,
    IS_CONFUSED
}
