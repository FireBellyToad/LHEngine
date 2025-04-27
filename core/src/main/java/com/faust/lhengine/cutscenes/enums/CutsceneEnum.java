package com.faust.lhengine.cutscenes.enums;

import com.badlogic.gdx.Screen;
import com.faust.lhengine.screens.impl.EndGameScreen;
import com.faust.lhengine.screens.impl.GameScreen;
import com.faust.lhengine.screens.impl.MenuScreen;

/**
 * Cutscenes Enum
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum CutsceneEnum {
    INTRO("intro", 5, GameScreen.class),
    ENDGAME("endgame", 4, EndGameScreen.class),
    CREDITS("credits", 3, MenuScreen.class),
    STORY("story", 5, MenuScreen.class);

    private final String key; // string key in messages.cutscenes
    private final int stepsNumber; // number of steps of the cutscene
    private final Class<? extends Screen> nextScreenClass; //Class of the next screen

    <T extends Screen>CutsceneEnum(String key, int stepsNumber, Class<T> nextScreenClass) {
        this.key = key;
        this.stepsNumber = stepsNumber;
        this.nextScreenClass = nextScreenClass;
    }

    public String getKey() {
        return key;
    }

    public int getStepsNumber() {
        return stepsNumber;
    }

    public Class<?> getNextScreenClass() {
        return nextScreenClass;
    }
}
