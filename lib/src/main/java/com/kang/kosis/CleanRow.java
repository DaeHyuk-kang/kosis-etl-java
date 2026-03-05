// src/main/java/com/kang/kosis/CleanRow.java
package com.kang.kosis;

public record CleanRow(
        String date,
        String region,
        Long crime,
        Long population
) {}
