package com.faust.lhengine.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.menu.enums.MenuItem;
import com.faust.lhengine.saves.AbstractSaveFileManager;
import com.faust.lhengine.utils.TextLocalizer;

import java.util.Map;
import java.util.Objects;

/**
 * Menu class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class Menu implements InputProcessor {

    private static final float FONT_SIZE = 0.5f;
    private static final float MENU_X_OFFSET = 50;
    private static final float MENU_Y_OFFSET = (float) (LHEngine.GAME_HEIGHT * 0.5);
    private static final float SPAN = 15;
    private static final String ARROW_CHARACTER = "~";

    private BitmapFont mainFont;

    private MenuItem currentMenu;
    private int selectedMenuVoice = 0;
    private boolean changeToGameScreen = false;
    private boolean changeToNextScreen = false;
    private boolean changeToIntroScreen = false;
    private boolean changeToCreditScreen = false;
    private boolean changeToStoryScreen = false;

    private final AbstractSaveFileManager saveFileManager;
    private final Sound voiceChange;
    private final Sound wrongVoice;

    public Menu(AbstractSaveFileManager saveFileManager, AssetManager assetManager) {
        this(saveFileManager, MenuItem.MAIN, assetManager);
    }

    public Menu(AbstractSaveFileManager saveFileManager, MenuItem currentMenu, AssetManager assetManager) {
        this.saveFileManager = saveFileManager;
        this.currentMenu = currentMenu;

        assetManager.load("sounds/SFX_UIGeneric13.ogg", Sound.class);
        assetManager.load("sounds/SFX_UIGeneric15.ogg", Sound.class);
        assetManager.finishLoading();

        this.voiceChange = assetManager.get("sounds/SFX_UIGeneric13.ogg");
        this.wrongVoice = assetManager.get("sounds/SFX_UIGeneric15.ogg");
    }

    public void loadFonts(AssetManager assetManager) {

        // Prepare font
        mainFont = assetManager.get("fonts/main_font.fnt");
        mainFont.getData().setScale(FONT_SIZE);
    }

    /**
     * Draw current Menu with localized voices
     *
     * @param batch
     * @param textLocalizer
     */
    public void drawCurrentMenuLocalized(SpriteBatch batch, TextLocalizer textLocalizer) {
        Objects.requireNonNull(batch);
        Objects.requireNonNull(currentMenu);
        Objects.requireNonNull(currentMenu.getSubItems()); // Must have subitems!

        float verticalOffset = 0f;

        //Title
        if (Objects.nonNull(currentMenu.getTitleMessageKey())){
            String titleString = textLocalizer.localizeFromKey("menu", currentMenu.getTitleMessageKey());

            long count = titleString.chars().filter(ch -> ch == '\n').count();
            verticalOffset = (SPAN * 2/3) * count;

            mainFont.draw(batch, titleString, MENU_X_OFFSET, MENU_Y_OFFSET);
        }


        MenuItem[] subItemsArray = currentMenu.getSubItems();
        for (int i = 0; i < subItemsArray.length; i++) {
            mainFont.draw(batch, textLocalizer.localizeFromKey("menu", subItemsArray[i].name()),
                    MENU_X_OFFSET, MENU_Y_OFFSET - (verticalOffset + (SPAN * (1 + i))));
        }
        //Draw arrow on selected option
        mainFont.draw(batch, ARROW_CHARACTER, MENU_X_OFFSET - 10, MENU_Y_OFFSET - (verticalOffset + (SPAN * (1 + this.selectedMenuVoice))));
    }

    /**
     * Draw current menu without localization
     *
     * @param batch
     */
    public void drawCurrentMenu(SpriteBatch batch) {
        Objects.requireNonNull(batch);
        Objects.requireNonNull(currentMenu);
        Objects.requireNonNull(currentMenu.getSubItems()); // Must have subitems!

        MenuItem[] subItemsArray = currentMenu.getSubItems();
        for (int i = 0; i < subItemsArray.length; i++) {
            mainFont.draw(batch, subItemsArray[i].name().replace('_', ' '),
                    MENU_X_OFFSET, MENU_Y_OFFSET - (SPAN * (1 + i)));
        }
        //Draw arrow on selected option
        mainFont.draw(batch, ARROW_CHARACTER, MENU_X_OFFSET - 10, MENU_Y_OFFSET - (SPAN * (1 + this.selectedMenuVoice)));
    }


    @Override
    public boolean keyDown(int keycode) {

        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.UP: {
                if (selectedMenuVoice > 0) {
                    selectedMenuVoice--;
                    voiceChange.play();
                }
                break;
            }
            case Input.Keys.S:
            case Input.Keys.DOWN: {
                if (selectedMenuVoice < currentMenu.getSubItems().length - 1) {
                    selectedMenuVoice++;
                    voiceChange.play();
                }
                break;
            }
            case Input.Keys.X:
            case Input.Keys.K:
            case Input.Keys.ENTER: {
                handleSelection();
                voiceChange.play();
                break;
            }
        }
        return true;
    }

    /**
     * Handles selection event
     */
    private void handleSelection() {
        switch (currentMenu) {
            case NEW_GAME:
                handleNewGame();
                break;
            case MAIN:
                handleMain();
                break;
            case PAUSE_GAME:
            case GAME_OVER:
                handleResumeGameOrStop();
                break;
            case PLAY_GAME:
                handlePlayGame();
                break;
            default:
                handleDefaults();
                break;
        }
    }

    private void handlePlayGame() {
        switch (selectedMenuVoice) {
            case 0: {
                //New game
                Map<String, Object> rawData = saveFileManager.loadRawValues();
                if (rawData == null || rawData.isEmpty()) {
                    changeToIntroScreen = true;
                } else {
                    selectedMenuVoice = 0;
                    currentMenu = MenuItem.NEW_GAME;
                }
                break;
            }
            case 1: {
                //Load game if present, else do nothing
                Map<String, Object> rawData = saveFileManager.loadRawValues();
                if (rawData == null || rawData.isEmpty()) {
                    wrongVoice.play();
                    selectedMenuVoice = 0;
                } else {
                    changeToGameScreen = true;
                }
                break;
            }
            case 2: {
                //Back
                selectedMenuVoice = 0;
                currentMenu = MenuItem.MAIN;
                break;
            }
        }
    }

    private void handleMain() {
        switch (selectedMenuVoice) {
            case 0: {
                //New game
                currentMenu = MenuItem.PLAY_GAME;
                break;
            }
            case 1: {
                //credit
                changeToCreditScreen = true;
                break;
            }
            case 2: {
                //credit
                changeToStoryScreen = true;
                break;
            }
            case 3: {
                //Exit game
                Gdx.app.exit();
                break;
            }
        }
    }

    private void handleNewGame() {
        switch (selectedMenuVoice) {
            case 0: {
                //Yes i'm sure
                saveFileManager.cleanSaveFile();
                changeToIntroScreen = true;
                break;
            }
            case 1: {
                //No, back to main
                currentMenu = MenuItem.MAIN;
                selectedMenuVoice = 0;
                break;
            }
        }
    }

    private void handleResumeGameOrStop() {
        switch (selectedMenuVoice) {
            case 0: {
                //Yes, continue game
                changeToGameScreen = true;
                break;
            }
            case 1: {
                //No, back to main
                changeToNextScreen = true;
                selectedMenuVoice = 0;
                break;
            }
        }
    }

    public void reset() {
        selectedMenuVoice = 0;
        changeToCreditScreen = false;
        changeToGameScreen = false;
        changeToIntroScreen = false;
        changeToNextScreen = false;
    }

    /**
     * Just go to next Screen
     */
    private void handleDefaults() {
        changeToNextScreen = true;
    }

    public int getSelectedMenuVoice() {
        return selectedMenuVoice;
    }

    public boolean isChangeToGameScreen() {
        return changeToGameScreen;
    }

    public boolean isChangeToIntroScreen() {
        return changeToIntroScreen;
    }

    public boolean isChangeToNextScreen() {
        return changeToNextScreen;
    }

    public boolean isChangeToCreditScreen() {
        return changeToCreditScreen;
    }
    public boolean isChangeToStoryScreen() {
        return changeToStoryScreen;
    }

    public BitmapFont getMainFont() {
        return mainFont;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return false;
    }

}
