package com.faust.lhengine.screens.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.faust.lhengine.game.scripts.enums.ScriptActorType;
import com.faust.lhengine.screens.AbstractScreen;
import com.faust.lhengine.utils.LoggerUtils;
import com.faust.lhengine.utils.ValidScript;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.music.MusicManager;

public class LoadingScreen extends AbstractScreen {

    private final MusicManager musicManager;
    private final Texture loadScreen;

    private final ShapeRenderer backgroundBox = new ShapeRenderer();
    private static final Color back = new Color(0x666666ff);

    private final ShapeRenderer cornerBox = new ShapeRenderer();
    private static final Color corner = new Color(0xffffffff);

    public LoadingScreen(LHEngine game) {
        super(game);
        musicManager = game.getMusicManager();
        loadScreen = assetManager.get("splash/loading_splash.png");
    }

    @Override
    public void show() {

        //Validate on the run
        try {
            ValidScript.validateAllScriptsGdx();
        } catch (Exception e) {
            //If validation fails, stop game
            throw new GdxRuntimeException(e);
        }

        assetManager.load("sprites/walfrit_sheet.png", Texture.class);
        assetManager.load("sprites/decorations_sheet.png", Texture.class);
        assetManager.load("sprites/poi_sheet.png", Texture.class);
        assetManager.load("sprites/shadow.png", Texture.class);
        assetManager.load("sprites/flying_corpse_sheet.png", Texture.class);
        assetManager.load("sprites/strix_sheet.png", Texture.class);
        assetManager.load("sprites/hive_sheet.png", Texture.class);
        assetManager.load("sprites/spitter_sheet.png", Texture.class);
        assetManager.load("sprites/meat_sheet.png", Texture.class);
        assetManager.load("sprites/portal_sheet.png", Texture.class);
        assetManager.load("sprites/tutorial_sheet.png", Texture.class);
        assetManager.load("sprites/willowisp_sheet.png",Texture.class);
        assetManager.load("sprites/escape_portal_sheet.png", Texture.class);
        assetManager.load("sprites/diaconus_sheet.png", Texture.class);

        assetManager.load("sounds/SFX_collect&bonus13.ogg", Sound.class);
        assetManager.load("sounds/SFX_hit&damage13.ogg", Sound.class);
        assetManager.load("sounds/SFX_hit&damage2.ogg", Sound.class);
        assetManager.load("sounds/SFX_shot4.ogg", Sound.class);
        assetManager.load("sounds/SFX_shot5.ogg", Sound.class);
        assetManager.load("sounds/SFX_shot10.ogg", Sound.class);
        assetManager.load("sounds/SFX_swordSwing.ogg", Sound.class);
        assetManager.load("sounds/SFX_waterSplash.ogg", Sound.class);
        assetManager.load("sounds/SFX_creatureDie4.ogg", Sound.class);
        assetManager.load("sounds/SFX_hit&damage6.ogg", Sound.class);
        assetManager.load("sounds/horror_scream.ogg", Sound.class);
        assetManager.load("sounds/death_scream.ogg", Sound.class);
        assetManager.load("sounds/spit.ogg", Sound.class);
        assetManager.load("sounds/evade.ogg", Sound.class);
        assetManager.load("sounds/rattling-bones.ogg", Sound.class);
        assetManager.load("sounds/terror.ogg", Sound.class);
        assetManager.load("sounds/SFX_flame1.ogg", Sound.class);
        assetManager.load("sounds/confusion_spell.ogg", Sound.class);

        assetManager.load("sprites/hud.png", Texture.class);
        assetManager.load("sprites/darkness_overlay.png", Texture.class);

        musicManager.loadMusicFromFiles(assetManager);

        JsonValue allSplashScreens = new JsonReader().parse(Gdx.files.internal("splash/splashScreen.json")).get("splashScreens");
        for (JsonValue splashInfo : allSplashScreens) {
            assetManager.load(splashInfo.getString("splashPath"), Texture.class);
        }

        for(ScriptActorType echoActor : ScriptActorType.values()){
            assetManager.load(echoActor.getSpriteFilename(), Texture.class);
        }
    }

    @Override
    public void render(float delta) {

        cameraManager.applyAndUpdate();
        game.getBatch().setProjectionMatrix(cameraManager.getCamera().combined);

        assetManager.update();
        if(assetManager.isFinished()){
            game.setScreen(new MenuScreen(game));
        }

        double loadingProgress = Math.floor(assetManager.getProgress()*100);

        //Load screen
        game.getBatch().begin();
        game.getBatch().draw(loadScreen,0,0);
        game.getBatch().end();

        //Black Corner
        game.getBatch().begin();
        backgroundBox.setColor(back);
        backgroundBox.setProjectionMatrix(cameraManager.getCamera().combined);
        backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
        backgroundBox.rect(10, LHEngine.GAME_HEIGHT/2-6, LHEngine.GAME_WIDTH-20,  10);
        backgroundBox.end();

        cornerBox.setColor(corner);
        cornerBox.setProjectionMatrix(cameraManager.getCamera().combined);
        cornerBox.begin(ShapeRenderer.ShapeType.Filled);
        cornerBox.rect(12, LHEngine.GAME_HEIGHT/2-5, (float) (LHEngine.GAME_WIDTH-25 - (100-loadingProgress)), 8);
        cornerBox.end();
        game.getBatch().end();

        Gdx.app.log(LoggerUtils.DEBUG_TAG, "Loading progress: " + loadingProgress + "%" );
    }
}
