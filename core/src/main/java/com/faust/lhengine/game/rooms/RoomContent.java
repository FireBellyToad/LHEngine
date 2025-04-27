package com.faust.lhengine.game.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.faust.lhengine.game.ai.RoomNodesGraph;
import com.faust.lhengine.game.instances.AnimatedInstance;
import com.faust.lhengine.game.instances.GameInstance;
import com.faust.lhengine.game.instances.impl.DecorationInstance;
import com.faust.lhengine.game.instances.impl.POIInstance;
import com.faust.lhengine.game.instances.impl.PlayerInstance;
import com.faust.lhengine.game.instances.impl.ScriptActorInstance;
import com.faust.lhengine.game.rooms.areas.EmergedArea;
import com.faust.lhengine.game.rooms.areas.TriggerArea;
import com.faust.lhengine.game.rooms.areas.WallArea;
import com.faust.lhengine.game.rooms.enums.RoomFlagEnum;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * Model class for storing Rooom contents
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class RoomContent {

    public TiledMap tiledMap;
    public List<POIInstance> poiList;
    public List<DecorationInstance> decorationList;
    public List<AnimatedInstance> enemyList;
    public List<WallArea> wallList;
    public List<EmergedArea> emergedAreaList;
    public PlayerInstance player;
    public RoomTypeEnum roomType;
    public String roomFileName;
    public List<ScriptActorInstance> echoActors; //Can have Echo actors
    public List<GameInstance> spellEffects;
    public List<POIInstance> removedPoiList;
    public List<TriggerArea> triggerAreaList;

    public Map<RoomFlagEnum, Boolean> roomFlags;
    public RoomNodesGraph roomGraph;
}
