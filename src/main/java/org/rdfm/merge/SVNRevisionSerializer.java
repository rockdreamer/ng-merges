package org.rdfm.merge;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.lang.reflect.Type;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class SVNRevisionSerializer implements JsonDeserializer<SVNRevision>, JsonSerializer<SVNRevision> {
    static final Logger log = LoggerFactory.getLogger(SVNRevisionSerializer.class);

    @Override
    public SVNRevision deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return SVNRevision.create(jsonElement.getAsNumber().longValue());
    }

    @Override
    public JsonElement serialize(SVNRevision svnurl, Type type, JsonSerializationContext jsonSerializationContext) {
       return new JsonPrimitive(svnurl.getNumber());
    }
}
