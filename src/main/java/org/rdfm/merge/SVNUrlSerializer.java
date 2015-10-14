package org.rdfm.merge;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class SVNUrlSerializer implements JsonDeserializer<SVNURL>, JsonSerializer<SVNURL> {
    static final Logger log = LoggerFactory.getLogger(SVNUrlSerializer.class);

    @Override
    public SVNURL deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return SVNURL.parseURIEncoded(jsonElement.getAsJsonPrimitive().getAsString());
        } catch (SVNException e) {
            log.error("Invalid SVN URL for JSon {}", jsonElement, e);
            return null;
        }
    }

    @Override
    public JsonElement serialize(SVNURL svnurl, Type type, JsonSerializationContext jsonSerializationContext) {
       return new JsonPrimitive(svnurl.toString());
    }
}
