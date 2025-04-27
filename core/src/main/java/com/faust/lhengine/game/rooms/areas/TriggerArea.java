package com.faust.lhengine.game.rooms.areas;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.faust.lhengine.game.gameentities.enums.ItemEnum;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.impl.POIInstance;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.rooms.enums.TriggerTypeEnum;

import java.util.List;
import java.util.Objects;

/**
 * Trigger area.
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class TriggerArea {

    private final long triggerId;
    private final TriggerTypeEnum triggerTypeEnum;
    private final List<ItemEnum> itemsNeededForTrigger;
    private boolean activated;
    private final GameInstance referencedInstance;

    private Body body;
    private final Rectangle triggerRect;

    public TriggerArea(long triggerId, TriggerTypeEnum triggerTypeEnum, List<ItemEnum> itemsNeededForTrigger, Rectangle wallRect, GameInstance referencedInstance) {
        this.triggerId = triggerId;
        this.triggerTypeEnum = triggerTypeEnum;
        this.itemsNeededForTrigger = itemsNeededForTrigger;
        this.triggerRect = wallRect;
        this.referencedInstance = referencedInstance;
        this.activated= false;
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
        shape.setAsBox(triggerRect.width / 2, triggerRect.height / 2);
        bodyDef.position.set(triggerRect.x + triggerRect.width / 2, triggerRect.y + triggerRect.height / 2);

        // Define Fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density =  0;
        fixtureDef.friction = 0;
        fixtureDef.isSensor = true;

        // Associate body to world
        body = world.createBody(bodyDef);
        body.setUserData(this);
        body.createFixture(fixtureDef);

        shape.dispose();
    }

    public long getTriggerId() {
        return triggerId;
    }

    public Body getBody() {
        return body;
    }

    /**
     * Check if can activate the trigger, and do that is needed
     *
     * @param player
     */
    public void activate(PlayerInstance player){

        //Has the player all the needed items (if any) to activate the trigger?
        final boolean missingNeededItem= !itemsNeededForTrigger.isEmpty() && itemsNeededForTrigger.stream().anyMatch(itemEnum -> player.getItemQuantityFound(itemEnum) == 0);

        //is referencedInstance a POI that has been used?
        final boolean referencedInstanceHasBeenUsed = referencedInstance instanceof POIInstance && ((POIInstance)referencedInstance).isAlreadyExamined();

        //If both are true, then activate the trigger
        this.activated = !missingNeededItem && (TriggerTypeEnum.CONTACT.equals(this.triggerTypeEnum)  || referencedInstanceHasBeenUsed);

    }

    public boolean isActivated() {
        return activated;
    }

    public GameInstance getReferencedInstance() {
        return referencedInstance;
    }
}
