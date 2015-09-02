package org.rdfm.merge;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by bantaloukasc on 27/08/15.
 */
public class ExceptionSerializer implements JsonSerializer<Throwable> {

    @Override
    public JsonElement serialize(Throwable e, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray representations = new JsonArray();
        Throwable currentException = e;
        for (int cEx = 0; cEx < 10; cEx++) {
            JsonObject exceptionRepresentation = new JsonObject();

            StackTraceElement[] st = currentException.getStackTrace();
            JsonArray stacktrace = new JsonArray();
            for (int i = 0; i < st.length; i++) {
                JsonObject repr = new JsonObject();
                repr.addProperty("className", st[i].getClassName());
                repr.addProperty("methodName", st[i].getMethodName());
                repr.addProperty("fileName", st[i].getFileName());
                repr.addProperty("lineNumber", st[i].getLineNumber());
                stacktrace.add(repr);
            }
            exceptionRepresentation.add("stackTrace", stacktrace);

            exceptionRepresentation.addProperty("message", currentException.getMessage());
            exceptionRepresentation.addProperty("localizedMessage", currentException.getLocalizedMessage());

            representations.add(exceptionRepresentation);

            currentException = e.getCause();
            if (currentException == null) {
                break;
            }
        }
        return representations;
    }
}
