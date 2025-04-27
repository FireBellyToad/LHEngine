package com.faust.lhengine.camera.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.faust.lhengine.LHEngine;

/**
 * Custom viewport for fullscreen handling
 */
public class LHITViewport extends ScalingViewport {

    public LHITViewport(Camera camera) {
        super(Scaling.none, LHEngine.GAME_WIDTH, LHEngine.GAME_HEIGHT, camera);
    }

    /**
     * Override update logic, using Scaling.fillY calculation but with floor to nearest integer
     * for fixing sprite tearing
     *
     * @param screenWidth
     * @param screenHeight
     * @param centerCamera
     */
    @Override
    public void update(final int screenWidth, final int screenHeight, final boolean centerCamera) {

        //Use fillY logic, but scaling down to nearest integer
        final Vector2 scaled = new Vector2();
        final float scale = screenHeight / (int) getWorldHeight();

        scaled.x = getWorldWidth() * scale;
        scaled.y = getWorldHeight() * scale;

        //Scale viewport
        final int viewportWidth = (int) Math.round(scaled.x);
        final int viewportHeight = (int) Math.round(scaled.y);

        //Normal flow from now on
        // Center.
        setScreenBounds((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2, viewportWidth, viewportHeight);

        apply(centerCamera);
    }
}
