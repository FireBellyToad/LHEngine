package com.faust.lhengine.mainworldeditor.serialization;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonSerializer;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.faust.lhengine.game.gameentities.enums.DirectionEnum;
import com.faust.lhengine.game.rooms.RoomModel;
import com.faust.lhengine.game.rooms.RoomPosition;
import com.faust.lhengine.game.rooms.enums.RoomTypeEnum;
import com.faust.lhengine.mainworldeditor.model.MainWorldData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main World Serializer class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MainWorldSerializer implements JsonSerializer<MainWorldData> {

    @Override
    public void write(Json json, MainWorldData mainWorldModel, Class knownType) {

        json.writeObjectStart();
        json.writeArrayStart("terrains");

        for (Map.Entry<RoomPosition, RoomModel> entry : mainWorldModel.terrains.entrySet()) {

            json.writeObjectStart();
            json.writeValue("roomType", entry.getValue().type.name());
            json.writeValue("x", entry.getKey().getX());
            json.writeValue("y", entry.getKey().getY());

            //Boundaries
            if (!entry.getValue().boundaries.isEmpty()) {

                json.writeArrayStart("boundaries");

                for (Map.Entry<DirectionEnum, RoomPosition> boundary : entry.getValue().boundaries.entrySet()) {

                    json.writeObjectStart();
                    json.writeValue("side",boundary.getKey().name());

                    if (Objects.isNull(boundary.getValue())) {
                        json.writeValue("target", (Object) null);
                    } else {
                        json.writeObjectStart("target");
                        json.writeValue("x", boundary.getValue().getX());
                        json.writeValue("y", boundary.getValue().getY());
                        json.writeObjectEnd();
                    }
                    json.writeObjectEnd();
                }
                json.writeArrayEnd();
            }
            json.writeObjectEnd();
        }
        json.writeArrayEnd();
        json.writeObjectEnd();

    }

    @Override
    public MainWorldData read(Json json, JsonValue mainWorldJson, Class type) {
        var mainWorldModel = new MainWorldData();
        for (JsonValue terrain : mainWorldJson.get("terrains")) {

            var roomPosition = new RoomPosition(terrain.getInt("x"), terrain.getInt("y"));
            var roomType = RoomTypeEnum.valueOf(terrain.getString("roomType"));
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
