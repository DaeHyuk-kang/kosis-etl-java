// src/main/java/com/kang/kosis/Transformer.java
package com.kang.kosis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * KOSIS raw(JSON array) -> 최신연도 지역별 CleanRow(date, region, value, population)
 *
 * raw 기준 규칙:
 * - date        : PRD_DE (연도)
 * - region      : C2_NM (서울/부산/...)
 * - label       : C1_NM ("범죄발생총건수(A)" or "인구(B)")
 * - number      : DT
 */
public class Transformer {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final class Bucket {
        final String year;
        final String region;
        Long crime;
        Long population;

        Bucket(String year, String region) {
            this.year = year;
            this.region = region;
        }
    }

    public static List<CleanRow> transform(String rawJson) throws IOException {
        JsonNode node = OM.readTree(rawJson);
        if (node == null) return List.of();

        // object면 1건 배열로 취급
        if (node.isObject()) {
            List<JsonNode> one = List.of(node);
            return transformNodes(one);
        }

        if (!node.isArray()) {
            throw new IllegalArgumentException("Unexpected JSON type: " + node.getNodeType());
        }

        List<JsonNode> nodes = new ArrayList<>();
        node.forEach(nodes::add);
        return transformNodes(nodes);
    }

    private static List<CleanRow> transformNodes(List<JsonNode> rows) {
        // 1) 최신 연도
        String latestYear = null;
        for (JsonNode row : rows) {
            String y = extractYear(text(row, "PRD_DE"));
            if (y == null) continue;
            if (latestYear == null || y.compareTo(latestYear) > 0) latestYear = y;
        }
        if (latestYear == null) return List.of();

        // 2) 최신연도만 bucket
        Map<String, Bucket> map = new LinkedHashMap<>();

        for (JsonNode row : rows) {
            if (row == null || !row.isObject()) continue;

            String year = extractYear(text(row, "PRD_DE"));
            if (year == null || !year.equals(latestYear)) continue;

            String region = trimToNull(text(row, "C2_NM"));
            String label  = trimToNull(text(row, "C1_NM"));
            Long dt       = parseLongLoose(text(row, "DT"));

            if (region == null || label == null || dt == null) continue;

            String key = year + "|" + region;
            Bucket b = map.computeIfAbsent(key, k -> new Bucket(year, region));

            if (isCrimeLabel(label)) {
                b.crime = dt;
            } else if (label.contains("인구")) {
                b.population = dt;
            }
        }

        // 3) CleanRow
        List<CleanRow> out = new ArrayList<>(map.size());
        for (Bucket b : map.values()) {
            out.add(new CleanRow(b.year, b.region, b.crime, b.population));
        }
        return out;
    }

    private static boolean isCrimeLabel(String label) {
        return label.contains("범죄발생총건수") || (label.contains("범죄") && label.contains("총건수"));
    }

    private static String text(JsonNode obj, String key) {
        JsonNode v = obj.get(key);
        return v == null ? null : v.asText(null);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String extractYear(String d) {
        if (d == null) return null;
        String t = d.trim();
        if (t.length() >= 4) {
            String y = t.substring(0, 4);
            if (y.chars().allMatch(Character::isDigit)) return y;
        }
        return null;
    }

    private static Long parseLongLoose(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;

        t = t.replaceAll("[,\\s]", "");
        int dot = t.indexOf('.');
        if (dot >= 0) t = t.substring(0, dot);

        try {
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
