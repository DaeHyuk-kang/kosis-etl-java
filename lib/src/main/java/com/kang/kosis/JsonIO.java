// src/main/java/com/kang/kosis/JsonIO.java
package com.kang.kosis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonIO {

    private static final ObjectMapper OM = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void writePrettyJson(Path path, String body) throws IOException {
        Files.writeString(path, prettyOrRaw(body), StandardCharsets.UTF_8);
    }

    public static void writeJson(Path path, Object obj) throws IOException {
        Files.writeString(path, OM.writeValueAsString(obj), StandardCharsets.UTF_8);
    }

    private static String prettyOrRaw(String body) {
        if (body == null) return "";
        try {
            JsonNode n = OM.readTree(body);
            return OM.writeValueAsString(n);
        } catch (Exception e) {
            return body;
        }
    }
}
