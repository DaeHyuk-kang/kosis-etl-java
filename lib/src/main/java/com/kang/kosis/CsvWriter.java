// src/main/java/com/kang/kosis/CsvWriter.java
package com.kang.kosis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter {

    public static void write(List<CleanRow> rows, Path out) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("date,region,value,population\n");

        if (rows != null) {
            for (CleanRow r : rows) {
                sb.append(escape(r.date())).append(",");
                sb.append(escape(r.region())).append(",");
                sb.append(numOrBlank(r.crime())).append(",");
                sb.append(numOrBlank(r.population())).append("\n");
            }
        }

        Files.writeString(out, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String numOrBlank(Long v) {
        return v == null ? "" : v.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        boolean needQuote = s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        if (!needQuote) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
