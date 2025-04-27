package com.faust.lhengine.utils.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.rooms.MainWorldModel;
import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main World Serializer class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MainWorldSerializer implements Json.Serializer<MainWorldModel> {

    @Override
    public void write(Json json, MainWorldModel mainWorldModel, Class knownType) {

        json.writeValue("terrains", (Object) null);
        throw new UnsupportedOperationException("");
    }

    @Override
    public MainWorldModel read(Json json, JsonValue mainWorldJson, Class type) {
        MainWorldModel mainWorldModel = new MainWorldModel();
        for (JsonValue terrain : mainWorldJson.get("terrains")) {

            RoomPosition roomPosition = new RoomPosition(terrain.getInt("x"), terrain.getInt("y"));
            RoomTypeEnum roomType = RoomTypeEnum.valueOf(terrain.getString("roomType"));
            Objects.requireNonNull(roomType);

            //Parsing boundaries
            JsonValue boundariesJson = terrain.get("boundaries");
            Map<DirectionEnum, RoomPosition> boundaries = new HashMap<>();
            if (Objects.nonNull(boundariesJson)) {
                boundariesJson.forEach((b) -> {
                    //Parsing targets
                    JsonValue targetJson = b.get("target");
                    //Null if impassable
                    RoomPosition target = null;
                    if (Objects.nonNull(targetJson.child)) {
                        target = new RoomPosition(targetJson.getInt("x"), targetJson.getInt("y"));
                    }
                    DirectionEnum side = DirectionEnum.valueOf(b.getString("side"));
                    Objects.requireNonNull(side);
                    boundaries.put(side, target);
                });
            }

            mainWorldModel.terrains.put(roomPosition, new RoomModel(boundaries, roomType));
        }

        return mainWorldModel;
    }
}
