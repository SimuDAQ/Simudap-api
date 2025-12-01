package com.simudap.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TypeConverter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private TypeConverter() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Integer toInt(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.trim().isBlank())
                .map(Integer::parseInt)
                .orElse(null);
    }

    public static Long toLong(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.trim().isBlank())
                .map(Long::parseLong)
                .orElse(null);
    }

    public static Double toDouble(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(Double::parseDouble)
                .orElse(null);
    }

    public static LocalDateTime toDateTime(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.trim().isBlank())
                .map(v -> LocalDate.parse(v, DATE_FORMAT).atStartOfDay())
                .orElse(null);
    }
}
