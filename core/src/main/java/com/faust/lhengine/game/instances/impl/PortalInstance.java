
package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.LHEngine;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.impl.PortalEntity;

import java.util.Objects;

/**
 * @author Portal Instance class
 */
public class PortalInstance extends AnimatedInstance {

    public PortalInstance(AssetManager assetManager) {
        super(new PortalEntity(assetManager));
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {
        //Nothing to do here...
    }

    private float mapStateTimeFromBehaviour(float stateTime) {
        return stateTime;
    }

    @Override
    public void createBody(World world, float x, float y) {
        //not needed!
    }

    @Override
    public void draw(SpriteBatch batch, float stateTime) {
        //is fixed;
        Objects.requireNonNull(batch);
        batch.begin();
        TextureRegion frame = ((AnimatedEntity) entity).getFrame(getCurrentBehavior(), mapStateTimeFromBehaviour(stateTime), true);
        batch.draw(frame, 80 - POSITION_OFFSET , LHEngine.GAME_HEIGHT- POSITION_OFFSET - 108);
        batch.end();
    }
}
