package com.simudap.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    private static final ZoneId seoul = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DATE_TIME_FORMATTER2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER3 = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    public static LocalDateTime toLocalDateTime1(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER1);
    }

    public static LocalDateTime toLocalDateTime2(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER2);
    }

    public static LocalDateTime toLocalDateTime3(String dateTime) {
        LocalDate localDate = LocalDate.parse(dateTime, DATE_TIME_FORMATTER3);
        return localDate.atStartOfDay();
    }

    public static LocalDateTime seoulNow() {
        return LocalDateTime.now(seoul);
    }

    public static String toDateString(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER3);
    }

    public static String toTimeString(LocalDateTime dateTime) {
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Epoch(1970-01-01 00:00)부터 주어진 시간까지의 총 분 수를 계산
     *
     * @param dateTime 계산할 시간 (로컬 시간 기준)
     * @return Epoch부터의 총 분 수
     */
    public static long toMinutesFromEpoch(LocalDateTime dateTime) {
        LocalDateTime epoch = LocalDate.ofEpochDay(0).atStartOfDay();
        return ChronoUnit.MINUTES.between(epoch, dateTime);
    }
}
