package com.faust.lhengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.faust.lhengine.camera.CameraManager;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.saves.AbstractSaveFileManager;
import com.faust.lhengine.screens.impl.LanguageScreen;
import com.faust.lhengine.utils.TextLocalizer;

public class LHEngine extends Game {

    public static final float GAME_WIDTH = 160;
    public static final float GAME_HEIGHT = 144;

    private final boolean isWebBuild;
    private SpriteBatch batch;
    private AssetManager assetManager;
    private CameraManager cameraManager;
    private final AbstractSaveFileManager saveFileManager;
    private MusicManager musicManager;
    private TextLocalizer textLocalizer;

    public LHEngine(boolean isWebBuild, AbstractSaveFileManager saveFileManager) {
        super();
        this.isWebBuild = isWebBuild;
        this.saveFileManager = saveFileManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        cameraManager = new CameraManager();
        musicManager = new MusicManager();

        assetManager.load("fonts/main_font.fnt", BitmapFont.class);
        assetManager.finishLoading();

        textLocalizer = new TextLocalizer();

        setScreen(new LanguageScreen(this));
    }

    @Override
    public void dispose() {

        getScreen().dispose();
        assetManager.dispose();
        cameraManager.dispose();
        batch.dispose();
    }

    public boolean isWebBuild() {
        return isWebBuild;
    }

    @Override
    public void resize(int width, int height) {
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public AbstractSaveFileManager getSaveFileManager() {
        return saveFileManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public TextLocalizer getTextLocalizer() {
        return textLocalizer;
    }
}
