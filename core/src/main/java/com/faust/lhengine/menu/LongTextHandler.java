package com.faust.lhengine.menu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.utils.TextLocalizer;

/**
 * Long text handler class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class LongTextHandler  {

    private static final float FONT_SIZE = 0.5f;
    private static final float X_OFFSET = 5;
    private static final float Y_OFFSET = 10;
    private final TextLocalizer textLocalizer;

    private final String longTextKey;

    private BitmapFont mainFont;
    private int currentStep = 0;

    public LongTextHandler(TextLocalizer textLocalizer, String longTextKey) {
        this.textLocalizer = textLocalizer;
        this.longTextKey = longTextKey;
    }

    /**
     * Load fonts from asset manager
     *
     * @param assetManager
     */
    public void loadFonts(AssetManager assetManager) {
        // Prepare font
        mainFont = assetManager.get("fonts/main_font.fnt");
        mainFont.getData().setScale(FONT_SIZE);
    }

    /**
     * Draws current text. Batch should begin before and end after this method call.
     * @param batch
     */
    public void drawCurrentText(SpriteBatch batch) {
        //TODO maybe should be nice to have fading text?
        mainFont.draw(batch, textLocalizer.localizeFromKey("cutscenes", longTextKey + ".text." + (currentStep + 1)),
                X_OFFSET, LHEngine.GAME_HEIGHT - Y_OFFSET);
    }

    /**
     * Increments the step
     */
    public void goToNextStep() {
        currentStep++;
    }

    public int getCurrentStep() {
        return currentStep;
    }
}
