// src/main/java/com/kang/kosis/KosisClient.java
package com.kang.kosis;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * KOSIS 호출 담당 (Extract)
 * - API Key 노출 방지(로그/예외 메시지에서 마스킹)
 * - 재시도(간단 백오프)
 * - 에러 응답 판별(오탐 줄이기)
 */
public class KosisClient {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String apiKey;
    private final AppConfig cfg;

    public KosisClient(String apiKey, AppConfig cfg) {
        this.apiKey = apiKey;
        this.cfg = cfg;
    }

    public record FetchResult(String urlMasked, String body) {}

    public FetchResult fetch() throws IOException, InterruptedException {
        final String url = buildUrl(apiKey, cfg);
        final String urlMasked = buildUrl("****", cfg);

        final int maxAttempts = 3;
        final long[] backoffMs = {200, 600, 1200};

        IOException lastIo = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println("[HTTP] GET " + urlMasked + " (attempt " + attempt + "/" + maxAttempts + ")");
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (res.statusCode() != 200) {
                    throw new IOException("HTTP " + res.statusCode() + " bodyHead=" + safeHead(res.body()));
                }

                String body = res.body();

                String err = detectKosisError(body);
                if (err != null) {
                    throw new IOException("KOSIS error response: " + err + " bodyHead=" + safeHead(body));
                }

                return new FetchResult(urlMasked, body);

            } catch (IOException e) {
                lastIo = e;
                if (attempt == maxAttempts) throw e;
            }

            Thread.sleep(backoffMs[attempt - 1]);
        }

        throw (lastIo != null) ? lastIo : new IOException("Unknown fetch failure");
    }

    private static String buildUrl(String apiKey, AppConfig cfg) {
        String base = "https://kosis.kr/openapi/statisticsData.do";
        return base
                + "?method=getList"
                + "&apiKey=" + urlEncode(apiKey)
                + "&format=json&jsonVD=Y"
                + "&userStatsId=" + urlEncode(cfg.userStatsId)
                + "&prdSe=" + urlEncode(cfg.prdSe)
                + "&newEstPrdCnt=" + cfg.newEstPrdCnt;
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String detectKosisError(String json) {
        try {
            JsonNode n = OM.readTree(json);

            if (!n.isObject()) return null;

            if (n.has("err") || n.has("error")) return "err/error";

            JsonNode r = n.get("RESULT");
            if (r != null && r.isObject()) {
                String code = textOrNull(r, "CODE", "code", "Code");
                String msg  = textOrNull(r, "MSG", "msg", "MESSAGE", "message");
                if (code != null && !code.isBlank() && !code.equalsIgnoreCase("INFO-000")) {
                    return "RESULT.CODE=" + code + (msg != null ? (", MSG=" + msg) : "");
                }
            }

            JsonNode r2 = n.get("result");
            if (r2 != null && r2.isObject()) {
                String code = textOrNull(r2, "CODE", "code");
                String msg  = textOrNull(r2, "MSG", "msg", "message");
                if (code != null && !code.isBlank() && !code.equalsIgnoreCase("INFO-000")) {
                    return "result.code=" + code + (msg != null ? (", msg=" + msg) : "");
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String textOrNull(JsonNode obj, String... keys) {
        for (String k : keys) {
            JsonNode v = obj.get(k);
            if (v != null && v.isValueNode()) {
                String s = v.asText();
                if (s != null) return s;
            }
        }
        return null;
    }

    private static String safeHead(String s) {
        if (s == null) return "";
        return s.substring(0, Math.min(200, s.length())).replace("\n", "\\n");
    }
}
