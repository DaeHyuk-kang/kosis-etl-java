package com.kang.kosis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppConfig {

    public final String userStatsId;
    public final String prdSe;
    public final int newEstPrdCnt;
    public final Path outDir;
    public final boolean saveCsv;

    public final String stamp;

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ⭐ 공개 예시 통계 ID (포트폴리오용)
    private static final String DEFAULT_STATS_ID =
            "PUBLIC_SAMPLE_STATS_ID";

    private AppConfig(String userStatsId,
                      String prdSe,
                      int newEstPrdCnt,
                      Path outDir,
                      boolean saveCsv) {

        this.userStatsId = userStatsId;
        this.prdSe = prdSe;
        this.newEstPrdCnt = newEstPrdCnt;
        this.outDir = outDir;
        this.saveCsv = saveCsv;
        this.stamp = LocalDateTime.now().format(TS);
    }

    public static AppConfig fromArgs(String[] args) throws Exception {

        ArgsMap a = ArgsMap.parse(args);

        String userStatsId = a.getOrDefault("userStatsId", DEFAULT_STATS_ID);

        // ⭐ 기본값 사용 안내
        if (DEFAULT_STATS_ID.equals(userStatsId)) {
            System.out.println(
                "[INFO] Using default public userStatsId. " +
                "Override with --userStatsId=YOUR_STATS_ID if needed."
            );
        }

        String prdSe = a.getOrDefault("prdSe", "Y");
        int newEstPrdCnt = a.getIntOrDefault("newEstPrdCnt", 3);
        Path outDir = Path.of(a.getOrDefault("outDir", "out"));
        boolean saveCsv = a.getBoolOrDefault("saveCsv", true);

        Files.createDirectories(outDir);

        return new AppConfig(userStatsId, prdSe, newEstPrdCnt, outDir, saveCsv);
    }
}
