package me.theeninja.pfflowing.gui;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;

import static me.theeninja.pfflowing.tournament.Round.*;

public class RoundSerializer implements JsonSerializer<Round> {
    @Override
    public JsonElement serialize(Round round, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        String roundName = round.getName();
        jsonObject.add(NAME, context.serialize(roundName));

        Side side = round.getSide();
        String sideName = side.name();
        jsonObject.add(SIDE, context.serialize(sideName));

        FlowGrid affirmativeFlowGrid = round.getAffController().flowGrid;
        jsonObject.add(AFF_FLOWING_GRID, context.serialize(affirmativeFlowGrid));

        FlowGrid negationFlowingGrid = round.getNegController().flowGrid;
        jsonObject.add(NEG_FLOWING_GRID, context.serialize(negationFlowingGrid));

        return jsonObject;
    }
}
