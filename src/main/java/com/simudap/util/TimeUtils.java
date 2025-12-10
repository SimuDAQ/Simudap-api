package com.simudap.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final ZoneId seoul = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    public static LocalDateTime toLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime seoulNow() {
        return LocalDateTime.now(seoul);
    }

    public static String toDateString(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    public static String toTimeString(LocalDateTime dateTime) {
        return dateTime.format(TIME_FORMATTER);
    }
}
