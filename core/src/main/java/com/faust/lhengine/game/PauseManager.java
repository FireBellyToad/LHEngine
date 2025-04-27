package com.faust.lhengine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.manager.RoomsManager;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.menu.Menu;
import com.faust.lhengine.menu.enums.MenuItem;
import com.faust.lhengine.saves.AbstractSaveFileManager;
import com.faust.lhengine.screens.impl.MenuScreen;
import com.faust.lhengine.utils.TextLocalizer;

/**
 * @author Jacopo "Faust" Buttiglieri
 */
public class PauseManager {

    private final Menu menu;
    private final AbstractSaveFileManager saveFileManager;
    private final TextLocalizer textLocalizer;
    private boolean gamePaused = false;
    private final MusicManager musicManager;

    private final ShapeRenderer backgroundBox = new ShapeRenderer();
    private static final Color back = new Color(0x000000ff);


    public PauseManager(AbstractSaveFileManager saveFileManager, MusicManager musicManager, AssetManager assetManager, TextLocalizer textLocalizer) {

        this.musicManager = musicManager;
        this.saveFileManager = saveFileManager;
        this.textLocalizer = textLocalizer;
        menu = new Menu(saveFileManager, MenuItem.PAUSE_GAME, assetManager);
        menu.loadFonts(assetManager);
    }

    /**
     *
     * @param game
     */
    public void doLogic(LHEngine game, PlayerInstance playerInstance, RoomsManager roomsManager){
        //Exit or resume game game
        if(menu.isChangeToGameScreen()){
            resumeGame();
        } else if (menu.isChangeToNextScreen()){
            saveFileManager.saveOnFile(playerInstance,roomsManager.getSaveMap());
            game.setScreen(new MenuScreen(game));
        }
    }

    public void draw(SpriteBatch batch, OrthographicCamera camera) {

        //Black Background
        batch.begin();
        backgroundBox.setColor(back);
        backgroundBox.setProjectionMatrix(camera.combined);
        backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
        backgroundBox.rect(0, (float) ((LHEngine.GAME_HEIGHT * 0.5)-40), LHEngine.GAME_WIDTH , 30);
        backgroundBox.end();
        batch.end();

        batch.begin();
        menu.drawCurrentMenuLocalized(batch, this.textLocalizer);
        batch.end();
    }

    /**
     * Pause and set as inputProcessor
     */
    public void pauseGame(){
        gamePaused = true;
        musicManager.pauseMusic();
        Gdx.input.setInputProcessor(menu);
    }

    /**
     * Unpause and reset menu
     */
    public void resumeGame(){
        gamePaused = false;
        musicManager.resumeMusic();
        menu.reset();
    }

    public boolean isGamePaused() {
        return gamePaused;
    }
}
