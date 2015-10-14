package org.rdfm.merge;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class FileDeSerializer implements JsonSerializer<File>, JsonDeserializer<File> {

    @Override
    public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new File(jsonElement.getAsJsonPrimitive().getAsString());
    }

    @Override
    public JsonElement serialize(File file, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(file.getAbsolutePath());
    }
}
