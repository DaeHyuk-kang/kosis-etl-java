// src/main/java/com/kang/kosis/ArgsMap.java
package com.kang.kosis;

import java.util.HashMap;
import java.util.Map;

/**
 * 간단 CLI 파서:
 *  --k=v, --flag (true)
 */
final class ArgsMap {
    private final Map<String, String> map;

    private ArgsMap(Map<String, String> map) {
        this.map = map;
    }

    static ArgsMap parse(String[] args) {
        Map<String, String> m = new HashMap<>();
        if (args != null) {
            for (String raw : args) {
                if (raw == null) continue;
                String a = raw.trim();
                if (!a.startsWith("--")) continue;

                int eq = a.indexOf('=');
                if (eq < 0) {
                    m.put(a.substring(2).trim(), "true");
                } else {
                    String k = a.substring(2, eq).trim();
                    String v = a.substring(eq + 1).trim();
                    if (!k.isEmpty()) m.put(k, v);
                }
            }
        }
        return new ArgsMap(m);
    }

    String getOrDefault(String key, String def) {
        String v = map.get(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    int getIntOrDefault(String key, int def) {
        String s = map.get(key);
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    boolean getBoolOrDefault(String key, boolean def) {
        String s = map.get(key);
        if (s == null || s.isBlank()) return def;
        String v = s.trim().toLowerCase();
        return switch (v) {
            case "true", "1", "y", "yes" -> true;
            case "false", "0", "n", "no" -> false;
            default -> def;
        };
    }
}
