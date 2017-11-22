package at.favre.app.blurbenchmark.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by PatrickF on 16.04.2014.
 */
public class JsonUtil {
    private static ObjectMapper defaultMapper = new ObjectMapper();

    public static String toJsonString(Object obj) {
        return toJsonString(obj, defaultMapper);
    }

    public static String toJsonString(Object obj, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        return fromJsonString(json, clazz, defaultMapper);
    }

    public static <T> T fromJsonString(String json, Class<T> clazz, ObjectMapper mapper) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonDeserializeException(e);
        }
    }

    public static class JsonSerializeException extends RuntimeException {
        public JsonSerializeException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class JsonDeserializeException extends RuntimeException {
        public JsonDeserializeException(Throwable throwable) {
            super(throwable);
        }
    }
}
