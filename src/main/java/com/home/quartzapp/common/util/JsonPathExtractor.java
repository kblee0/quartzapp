package com.home.quartzapp.common.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.Optional;

public class JsonPathExtractor {
    private final ReadContext ctx;

    public JsonPathExtractor(String json) {
        ctx = JsonPath.parse(json);
    }

    public static JsonPathExtractor parse(String json) {
        return new JsonPathExtractor(json);
    }

    public Optional<String> get(String jsonPath){
        if(ctx == null) return Optional.empty();

        Object obj = ctx.read(jsonPath);
        return Optional.ofNullable(obj instanceof String ? (String)obj : obj.toString());
    }
}
