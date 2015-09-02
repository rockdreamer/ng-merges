package org.rdfm.merge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class FileDeSerializer implements JsonDeserializer<File> {

    @Override
    public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new File(jsonElement.getAsJsonObject().get("path").getAsString());
    }
}
