package com.faust.lhengine.game.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.faust.lhengine.LHEngine;

import java.util.Objects;

/**
 * Darkness renderer class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class DarknessRenderer {

    private final Texture darknessOverlay;

    private final ShapeRenderer backgroundBox = new ShapeRenderer();
    private static final Color darkness = new Color(0x000000ff);

    public DarknessRenderer(AssetManager assetManager) {
        Objects.requireNonNull(assetManager);

        darknessOverlay = assetManager.get("sprites/darkness_overlay.png");

    }

    public void drawDarkness(SpriteBatch batch, Vector2 position, OrthographicCamera camera, boolean roundOffset) {
        Objects.requireNonNull(batch);
        Objects.requireNonNull(position);
        Objects.requireNonNull(camera);

        float xOffset = position.x + 6 - LHEngine.GAME_WIDTH / 2;
        float yOffset = position.y + 8 - LHEngine.GAME_HEIGHT / 2;

        if(roundOffset){
            xOffset = Math.round(xOffset);
            yOffset = Math.round(yOffset);
        }

        //Left overflow
        if (0 + xOffset > 0) {
            batch.begin();
            backgroundBox.setColor(darkness);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(0, 0, 0 + xOffset, LHEngine.GAME_HEIGHT - 12);
            backgroundBox.end();
            batch.end();
        }

        //Right overflow
        if (16 - xOffset > 0) {
            batch.begin();
            backgroundBox.setColor(darkness);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(xOffset + 144, 0, 16 - xOffset, LHEngine.GAME_HEIGHT - 12);
            backgroundBox.end();
            batch.end();
        }

        //Up overflow
        if (yOffset < 1) {
            batch.begin();
            backgroundBox.setColor(darkness);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(0 + xOffset, LHEngine.GAME_HEIGHT - 12, LHEngine.GAME_WIDTH + (16 - xOffset), yOffset);
            backgroundBox.end();
            batch.end();
        }

        //Down overflow
        if (yOffset > 0) {
            batch.begin();
            backgroundBox.setColor(darkness);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(0 + xOffset, 0, LHEngine.GAME_WIDTH + (16 - xOffset), yOffset);
            backgroundBox.end();
            batch.end();
        }

        //Darkness
        batch.begin();
        batch.draw(darknessOverlay, 0 + xOffset, 0 + yOffset);
        batch.end();
    }
}
