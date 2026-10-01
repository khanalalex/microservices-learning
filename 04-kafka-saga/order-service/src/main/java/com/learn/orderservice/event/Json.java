package com.learn.orderservice.event;

import tools.jackson.databind.json.JsonMapper;

// Kafka messages are just bytes. We turn events into JSON text and back ourselves.
public final class Json {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private Json() { }

    public static String toJson(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return MAPPER.readValue(json, type);
    }
}
