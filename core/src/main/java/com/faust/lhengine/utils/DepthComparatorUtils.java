package com.faust.lhengine.utils;

import com.faust.lhengine.game.gameentities.enums.POIEnum;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.impl.*;
import com.faust.lhengine.game.instances.interfaces.Hurtable;

public class DepthComparatorUtils {

    // Compares two GameInstances by y depth
    public static int compareEntities(GameInstance o1, GameInstance o2) {

        // PortalInstance has no body... yet
        if (o1 instanceof PortalInstance || o2 instanceof PortalInstance)
            return 0;

        //Special conditions for Echo Corpse spawned POI
        if (o2 instanceof POIInstance && POIEnum.ECHO_CORPSE.equals(((POIInstance) o2).getType()) && o1 instanceof DecorationInstance) {
            return -1;
        } else if (o1 instanceof POIInstance && POIEnum.ECHO_CORPSE.equals(((POIInstance) o1).getType()) && o2 instanceof DecorationInstance) {
            return 1;
        }

        //Special conditions to place object always on higher depth, usually
        //for avoiding that objects laying on the ground cover taller ones
        if ((o2 instanceof Hurtable && ((Hurtable) o2).isDead() && !(o2 instanceof PlayerInstance)) ||
                (o1 instanceof MonsterBirdInstance && ((MonsterBirdInstance) o1).isAttachedToPlayer()) ||
                (o2 instanceof DecorationInstance && o2.isAlwaysInBackground()) ||
                (o2 instanceof DecorationInstance && ((DecorationInstance) o2).getInteracted()) ||
                (o2 instanceof POIInstance && o2.isAlwaysInBackground())) {
            return 1;
        }

        if (((o1 instanceof Hurtable && ((Hurtable) o1).isDead()) && !(o1 instanceof PlayerInstance)) ||
                (o2 instanceof MonsterBirdInstance && ((MonsterBirdInstance) o2).isAttachedToPlayer()) ||
                (o1 instanceof DecorationInstance && o1.isAlwaysInBackground()) ||
                (o1 instanceof DecorationInstance && ((DecorationInstance) o1).getInteracted()) ||
                (o1 instanceof POIInstance && o1.isAlwaysInBackground())) {
            return -1;
        }

        //or else just sort by Y axis
        if ((o1.getBody().getPosition().y < o2.getBody().getPosition().y)) {
            return 1;
        } else if (o1.getBody().getPosition().y > o2.getBody().getPosition().y) {
            return -1;
        }

        return 0;
    }

}
