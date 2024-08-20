package com.faust.lhengine.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.camera.CameraManager;

/**
 * Abstact screen class to avoid too much empty methods around
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class AbstractScreen implements Screen {

    protected final LHEngine game;
    protected final CameraManager cameraManager;
    protected AssetManager assetManager;

    public AbstractScreen(LHEngine game) {
        this.game = game;
        this.cameraManager = game.getCameraManager();
        assetManager = game.getAssetManager();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        cameraManager.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
