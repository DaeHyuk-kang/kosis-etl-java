// src/main/java/com/kang/kosis/KosisRawToCleanApp.java
package com.kang.kosis;

import java.nio.file.Path;
import java.util.List;

public class KosisRawToCleanApp {

    public static void main(String[] args) throws Exception {
        AppConfig cfg = AppConfig.fromArgs(args);

        String apiKey = System.getenv("KOSIS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[ERROR] Missing env var: KOSIS_API_KEY");
            System.err.println("Example (macOS/Linux): export KOSIS_API_KEY=\"...\"");
            System.err.println("Example (Windows PowerShell): setx KOSIS_API_KEY \"...\"");
            System.exit(2);
        }

        System.out.println("[CONFIG] outDir=" + cfg.outDir.toAbsolutePath());
        System.out.println("[CONFIG] userStatsId=" + cfg.userStatsId);
        System.out.println("[CONFIG] prdSe=" + cfg.prdSe + ", newEstPrdCnt=" + cfg.newEstPrdCnt);
        System.out.println("[CONFIG] saveCsv=" + cfg.saveCsv);

        KosisClient client = new KosisClient(apiKey, cfg);

        // Extract
        KosisClient.FetchResult fetched = client.fetch();

        // Load(raw)
        Path rawPath = cfg.outDir.resolve("kosis_raw_" + cfg.stamp + ".json");
        JsonIO.writePrettyJson(rawPath, fetched.body());
        System.out.println("Saved raw   : " + rawPath.toAbsolutePath());

        // Transform
        List<CleanRow> cleaned = Transformer.transform(fetched.body());

        // Load(clean)
        Path cleanJsonPath = cfg.outDir.resolve("kosis_clean_" + cfg.stamp + ".json");
        JsonIO.writeJson(cleanJsonPath, cleaned);
        System.out.println("Saved clean : " + cleanJsonPath.toAbsolutePath());

        if (cfg.saveCsv) {
            Path cleanCsvPath = cfg.outDir.resolve("kosis_clean_" + cfg.stamp + ".csv");
            CsvWriter.write(cleaned, cleanCsvPath);
            System.out.println("Saved csv   : " + cleanCsvPath.toAbsolutePath());
        }

        System.out.println("[DONE] rows(clean)=" + cleaned.size());
    }
}
