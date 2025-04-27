package com.faust.lhengine.game.rooms.areas;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Objects;

/**
 * Static invisible Wall area.
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class WallArea {

    private Body body;
    private final Rectangle wallRect;

    public WallArea(Rectangle wallRect) {
        this.wallRect = wallRect;
    }

    public void dispose() {
        this.body.getFixtureList().forEach(f ->
                this.body.destroyFixture(f));
    }

    public void createBody(final World world) {
        Objects.requireNonNull(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;

        // Define shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(wallRect.width / 2, wallRect.height / 2);
        bodyDef.position.set(wallRect.x + wallRect.width / 2, wallRect.y + wallRect.height / 2);

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density =  1;
        fixtureDef.friction =  1;
        fixtureDef.isSensor = false;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        shape.dispose();
    }
}
