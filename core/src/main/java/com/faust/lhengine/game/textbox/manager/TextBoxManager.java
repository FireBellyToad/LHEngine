package com.faust.lhengine.game.textbox.manager;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Timer;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.textbox.TextBoxData;
import com.faust.lhengine.utils.TextLocalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Textboxes Manager
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class TextBoxManager {

    private static final float FONT_SIZE = 0.5f;
    private static final float MESSAGE_LIMIT = 1;
    private static final int TOTAL_TEXTBOX_HEIGHT = 17;

    private final BitmapFont mainFont;
    private final List<TextBoxData> textBoxes = new ArrayList<>();

    private final ShapeRenderer backgroundBox = new ShapeRenderer();
    private final ShapeRenderer cornerBox = new ShapeRenderer();
    private static final Color corner = new Color(0xffffffff);
    private static final Color back = new Color(0x222222ff);
    private Timer.Task currentTimer;
    private final TextLocalizer textLocalizer;

    public TextBoxManager(AssetManager assetManager, TextLocalizer textLocalizer) {

        // Prepare font
        mainFont = assetManager.get("fonts/main_font.fnt");
        mainFont.getData().setScale(FONT_SIZE);

        this.textLocalizer = textLocalizer;

    }

    /**
     * Generate a new text box with a time limit, after which it will be hidden from game screen
     *
     * @param textKey the key of the message to be shown
     */
    public void addNewTimedTextBox(final String textKey) {
        this.addNewTextBox(textKey);

        TextBoxData newText = textBoxes.get(textBoxes.size() - 1);

        // Hide box after time
        currentTimer = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!textBoxes.isEmpty()) {
                    textBoxes.remove(newText);
                    currentTimer.cancel();
                    currentTimer = null;
                }
            }
        }, newText.getTimeToShow());
    }

    /**
     * Generate a new text box
     *
     * @param textKey the key of the message to be shown
     */
    public void addNewTextBox(String textKey) {
        Objects.requireNonNull(textKey);

        if (textBoxes.size() == MESSAGE_LIMIT) {
            textBoxes.remove(0);
        }

        if (Objects.nonNull(currentTimer)) {
            currentTimer.cancel();
            currentTimer = null;
        }

        //Create text box given a textKey.
        TextBoxData newText = new TextBoxData(textLocalizer.localizeFromKey("boxes",textKey));
        textBoxes.add(newText);
    }

    /**
     * Render all the generated (right now just one) text boxes
     *
     * @param batch
     * @param camera
     */
    public void renderTextBoxes(final SpriteBatch batch, OrthographicCamera camera) {

        // Remove box if player is under a certain boundary and there is no splash screen
        int textLines;
        float fontY;
        float innerBoxHeight;
        float outerBoxHeight;

        //Render all the created boxes
        for (TextBoxData box : textBoxes) {

            //Adjust rendering if text has only one line
            textLines = box.getText().split("\n").length;
            outerBoxHeight =  TOTAL_TEXTBOX_HEIGHT * textLines;
            innerBoxHeight =  (TOTAL_TEXTBOX_HEIGHT * textLines) - 4;
            fontY = (TOTAL_TEXTBOX_HEIGHT * textLines) - (4+textLines*2); //Dynamic offset

            //White Corner
            batch.begin();
            cornerBox.setColor(corner);
            cornerBox.setProjectionMatrix(camera.combined);
            cornerBox.begin(ShapeRenderer.ShapeType.Filled);
            cornerBox.rect(0, 0, LHEngine.GAME_WIDTH, outerBoxHeight);
            cornerBox.end();

            //Black Background
            backgroundBox.setColor(back);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(2, 2, LHEngine.GAME_WIDTH - 4, innerBoxHeight);
            backgroundBox.end();
            batch.end();

            //Text
            batch.begin();
            mainFont.draw(batch, box.getTextIncremental(), 6, fontY);
            batch.end();
        }
    }

    public void dispose() {
        mainFont.dispose();
    }

    /**
     * @return
     */
    public BitmapFont getMainFont() {
        return mainFont;
    }

    /**
     * Removes all boxes
     */
    public void removeAllBoxes() {
        textBoxes.clear();
    }

    /**
     *
     * @return true if no boxes to draw are added
     */
    public boolean hasNoBoxesToDraw() {
        return textBoxes.isEmpty();
    }

}

