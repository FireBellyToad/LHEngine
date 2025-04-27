package com.faust.lhengine.screens.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.cutscenes.CutsceneManager;
import com.faust.lhengine.game.music.MusicManager;
import com.faust.lhengine.game.music.enums.TuneEnum;
import com.faust.lhengine.cutscenes.enums.CutsceneEnum;
import com.faust.lhengine.screens.AbstractScreen;

import java.util.Objects;

/**
 * Intro screen class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class CutsceneScreen extends AbstractScreen {

    private static final Color back = new Color(0x000000ff);

    private final MusicManager musicManager;
    private final ShapeRenderer background;

    private final CutsceneManager cutsceneManager;
    private final Screen nextScreen;

    private float stateTime = 0f;

    public CutsceneScreen(LHEngine game, CutsceneEnum cutsceneEnum) {
        super(game);

        musicManager = game.getMusicManager();

        musicManager.loadSingleTune(TuneEnum.DANGER, game.getAssetManager());
        background = new ShapeRenderer();

        //Init cutscene
        Objects.requireNonNull(cutsceneEnum);

        cutsceneManager = new CutsceneManager(cutsceneEnum, game.getAssetManager(), game.getTextLocalizer(),cameraManager.getCamera(),  game.getSaveFileManager(), game.isWebBuild());

        if(!CutsceneEnum.STORY.equals(cutsceneEnum)){
            //Loop title music
            musicManager.playMusic(TuneEnum.DANGER);
        }

        try {
            //Instantiate next screen using reflection
            //Using ClassReflection wrapper for html support
            Constructor screenConstructor =  ClassReflection.getDeclaredConstructor(cutsceneEnum.getNextScreenClass(), LHEngine.class);
            this.nextScreen = (Screen) screenConstructor.newInstance(game);
        } catch (Exception e) {
            throw new GdxRuntimeException(e);
        }

    }

    @Override
    public void show() {
        cutsceneManager.initCutscene();

        Gdx.input.setInputProcessor(cutsceneManager);
    }

    @Override
    public void render(float delta) {

        if (cutsceneManager.isFinished()) {
            //Stop music and change screen
            musicManager.stopMusic();
            game.setScreen(nextScreen);
        } else {
            cameraManager.applyAndUpdate();
            game.getBatch().setProjectionMatrix(cameraManager.getCamera().combined);

            stateTime += Gdx.graphics.getDeltaTime();
            Gdx.gl.glClearColor(0f, 0f, 0f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            renderBlackBackground();

            //intro screen render
            cutsceneManager.draw(game.getBatch(), stateTime, cameraManager.getCamera());
        }

    }

    private void renderBlackBackground() {

        game.getBatch().begin();
        background.setColor(back);
        background.setProjectionMatrix(cameraManager.getCamera().combined);
        background.begin(ShapeRenderer.ShapeType.Filled);
        background.rect(0, 0, LHEngine.GAME_WIDTH, LHEngine.GAME_HEIGHT);
        background.end();
        game.getBatch().end();
    }

}