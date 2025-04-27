package com.faust.lhengine.game.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.SpriteEntity;
import com.faust.lhengine.game.gameentities.enums.GameBehavior;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.hud.enums.HudIconsEnum;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.textbox.manager.TextBoxManager;
import com.faust.lhengine.screens.impl.GameScreen;

import java.util.Objects;

/**
 * Hud class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class Hud {

    private final SpriteEntity hudTexture;

    private final TextBoxManager textManager;

    private final Vector2 meterPosition = new Vector2(2.5f, LHEngine.GAME_HEIGHT - 10);
    private final Vector2 healthKitCountPosition = new Vector2(LHEngine.GAME_WIDTH - 10, LHEngine.GAME_HEIGHT - 4);
    private final Vector2 holyLancePiecesPosition = new Vector2(LHEngine.GAME_WIDTH - 30, LHEngine.GAME_HEIGHT - 4);
    private final Vector2 goldcrossCountPosition = new Vector2(LHEngine.GAME_WIDTH - 50, LHEngine.GAME_HEIGHT - 4);

    private final ShapeRenderer backgroundBox = new ShapeRenderer();
    private static final Color back = new Color(0x222222ff);

    //Healing timer bar
    private final ShapeRenderer cornerBox = new ShapeRenderer();
    private static final Color corner = new Color(0xffffffff);
    private boolean mustFlicker = false;
    private long startTime = 0;

    public Hud(TextBoxManager textManager, AssetManager assetManager) {
        Objects.requireNonNull(textManager);
        Objects.requireNonNull(assetManager);

        this.textManager = textManager;
        this.hudTexture = new SpriteEntity(assetManager.get("sprites/hud.png")) {
            @Override
            protected int getTextureColumns() {
                return HudIconsEnum.values().length;
            }

            @Override
            protected int getTextureRows() {
                return 1;
            }
        };

    }

    public void drawHud(SpriteBatch batch, PlayerInstance player, OrthographicCamera camera) {
        Objects.requireNonNull(batch);
        Objects.requireNonNull(player);
        Objects.requireNonNull(camera);

        Vector2 playerPosition = player.getBody().getPosition();

        //Black Background
        batch.begin();
        backgroundBox.setColor(back);
        backgroundBox.setProjectionMatrix(camera.combined);
        backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
        backgroundBox.rect(0, LHEngine.GAME_HEIGHT, LHEngine.GAME_WIDTH, -12);
        backgroundBox.end();
        batch.end();

        batch.begin();
        // If not hurt or the flickering POI must be shown, draw the texture
        if (!mustFlicker || !player.isDying()) {
            // Draw Health meter (red crosses for each hitponit remaining, hollow ones for each damage point)
            TextureRegion frame;
            for (int r = 0; r < player.getResistance(); r++) {
                frame = hudTexture.getFrame(r < player.getDamageDelta() ?
                        HudIconsEnum.LIFE_METER_FULL.ordinal() :
                        HudIconsEnum.LIFE_METER_EMPTY.ordinal() * AnimatedEntity.FRAME_DURATION);
                batch.draw(frame, meterPosition.x + (r * frame.getRegionWidth()), meterPosition.y);
            }
        }

        // Every 1/8 seconds alternate between showing and hiding the texture to achieve flickering effect
        if (player.isDying() && TimeUtils.timeSinceNanos(startTime) > GameScreen.FLICKER_DURATION_IN_NANO) {
            mustFlicker = !mustFlicker;

            // restart flickering timer
            startTime = TimeUtils.nanoTime();
        }


        //crosses found count. Set in red if all has been found
        batch.draw(hudTexture.getFrame(HudIconsEnum.GOLDCROSS.ordinal() * AnimatedEntity.FRAME_DURATION),
                goldcrossCountPosition.x - 10,
                goldcrossCountPosition.y - 6);

        textManager.getMainFont().draw(batch,
                String.valueOf(player.getItemQuantityFound(ItemEnum.GOLDCROSS)),
                goldcrossCountPosition.x,
                goldcrossCountPosition.y);

        //Holy lance pieces found count. Set in red if all has been found
        batch.draw(hudTexture.getFrame(HudIconsEnum.LANCE.ordinal() * AnimatedEntity.FRAME_DURATION),
                holyLancePiecesPosition.x - 10,
                holyLancePiecesPosition.y - 6);

        textManager.getMainFont().draw(batch,
                String.valueOf(player.getItemQuantityFound(ItemEnum.HOLY_LANCE)),
                holyLancePiecesPosition.x,
                holyLancePiecesPosition.y);

        //Healthkits found count
        batch.draw(hudTexture.getFrame(HudIconsEnum.HERBS.ordinal() * AnimatedEntity.FRAME_DURATION),
                healthKitCountPosition.x - 10,
                healthKitCountPosition.y - 6);

        textManager.getMainFont().draw(batch,
                String.valueOf(player.getAvailableHealthKits()),
                healthKitCountPosition.x,
                healthKitCountPosition.y);

        batch.end();

        //Draw Healing timer bar if player is curing himself
        if (GameBehavior.KNEE.equals(player.getCurrentBehavior())) {
            //Black Corner
            batch.begin();
            backgroundBox.setColor(back);
            backgroundBox.setProjectionMatrix(camera.combined);
            backgroundBox.begin(ShapeRenderer.ShapeType.Filled);
            backgroundBox.rect(playerPosition.x, playerPosition.y+17, 10,  5);
            backgroundBox.end();

            //White bar
            long deltaTimer = (TimeUtils.nanosToMillis(player.getStartHealingTime()) - ( TimeUtils.nanoTime() / 1000000) )/1000;

            cornerBox.setColor(corner);
            cornerBox.setProjectionMatrix(camera.combined);
            cornerBox.begin(ShapeRenderer.ShapeType.Filled);
            cornerBox.rect(playerPosition.x + 1, playerPosition.y + 18,8 + (deltaTimer * 2), 3);
            cornerBox.end();
            batch.end();
        }
    }
}
