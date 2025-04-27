package com.faust.lhengine.game.instances.impl;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.faust.lhengine.game.gameentities.AnimatedEntity;
import com.faust.lhengine.game.gameentities.SpriteEntity;
import com.faust.lhengine.game.gameentities.enums.DecorationsEnum;
import com.faust.lhengine.game.gameentities.impl.DecorationEntity;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.interfaces.Interactable;
import com.faust.lhengine.game.rooms.RoomContent;
import com.faust.lhengine.game.world.manager.CollisionManager;

import java.util.Objects;

/**
 * Class for Decoration Instances
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class DecorationInstance extends GameInstance implements Interactable {

    private final int decoIdInMap; // Decoration id in map

    private boolean interacted = false;

    public DecorationInstance(float x, float y, int id,  DecorationsEnum type, AssetManager assetManager) {
        super(new DecorationEntity(type, assetManager));
        // Add Position Y offset for better position from tiled
        this.startX = x;
        this.startY = y + POSITION_OFFSET;
        this.alwaysInBackground = type.equals(DecorationsEnum.ALLY_CORPSE_1) || type.equals(DecorationsEnum.ALLY_CORPSE_2);
        this.decoIdInMap = id;
    }

    @Override
    public void draw(SpriteBatch batch, float stateTime) {
        Objects.requireNonNull(batch);
        batch.begin();
        TextureRegion frame;

        if (interacted) {
            frame = ((SpriteEntity) entity).getFrame(AnimatedEntity.FRAME_DURATION);
        } else {
            frame = ((SpriteEntity) entity).getFrame(0);
        }

        Vector2 drawPosition = adjustPosition();
        //Rivedere
        batch.draw(frame, drawPosition.x + calculateAdditionalXOffset() - POSITION_OFFSET,
                drawPosition.y + calculateAdditionalYOffset() - POSITION_Y_OFFSET);
        batch.end();
    }

    /**
     * @return X offset for particular Decoration render
     */
    private int calculateAdditionalXOffset() {

        if (DecorationsEnum.CROSS_IRON.equals(((DecorationEntity) entity).getType())) {
            return 2;
        }
        return 0;
    }

    /**
     * @return Y offset for particular Decoration render
     */
    private int calculateAdditionalYOffset() {

        if (DecorationsEnum.BOAT.equals(((DecorationEntity) entity).getType())) {
            return -4;
        }
        if (DecorationsEnum.PAPER.equals(((DecorationEntity) entity).getType())) {
            return -8;
        }
        return 0;
    }

    @Override
    public void doLogic(float stateTime, RoomContent roomContent) {
        //Nothing to do here... yet
    }

    @Override
    public void createBody(final World world, float x, float y) {
        Objects.requireNonNull(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;

        // Define shape
        PolygonShape shape = new PolygonShape();

        //Adjustements
        switch (((DecorationEntity) entity).getType()) {
            case CROSS_IRON:
            case DEAD_TREE:
            case IMPALED: {
                shape.setAsBox(2, 2);
                bodyDef.position.set(x - 2, y - 16);
                break;
            }
            case PLANT:
            case PAPER: {
                shape.setAsBox(5, 3);
                bodyDef.position.set(x, y - 16);
                break;
            }
            case BOAT: {
                shape.setAsBox(12, 6);
                bodyDef.position.set(x, y - 12);
                break;
            }
            case STONE_1:
            case STONE_2:
            case BIG_TREE:
            case TREE_STUMP: {
                shape.setAsBox(16, 8);
                bodyDef.position.set(x, y - 16);
                break;
            }
            default: {
                shape.setAsBox(4, 2);
                bodyDef.position.set(x, y - 16);
                break;
            }
        }

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = isPassable() ? 0 : 1;
        fixtureDef.friction = isPassable() ? 0 : 1;
        fixtureDef.isSensor = isPassable();
        fixtureDef.filter.categoryBits = CollisionManager.SOLID_GROUP;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        shape.dispose();
    }

    public boolean isPassable() {
        return ((DecorationEntity) entity).isPassable();
    }

    public boolean getInteracted() {
        return this.interacted;
    }


    @Override
    public void doPlayerInteraction(PlayerInstance playerInstance) {

        switch (((DecorationEntity) entity).getType()) {
            case PLANT:
            case PAPER:
            case DEAD_TREE:
            case GRASS: {
                ((DecorationEntity) entity).playGrassMove();
            }
            default: {
                //Do nothing
            }
        }

        this.interacted = true;
    }

    @Override
    public void endPlayerInteraction(PlayerInstance playerInstance) {
        this.interacted = false;
    }

    public int getDecoIdInMap() {
        return decoIdInMap;
    }
}
