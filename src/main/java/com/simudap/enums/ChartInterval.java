package com.simudap.enums;

import com.simudap.error.ResourceNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChartInterval {
    MIN_TODAY(null),
    MIN_PAST(null),
    DAY("D"),
    WEEK("W"),
    MONTH("M"),
    YEAR("Y"),
    ;

    private final String value;

    public static ChartInterval from(String interval, boolean isToday) {
        if (interval.equalsIgnoreCase("min")) {
            return isToday ? MIN_TODAY : MIN_PAST;
        }

        return Arrays.stream(ChartInterval.values())
                .filter(value -> value.name().equalsIgnoreCase(interval))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found interval " + interval));
    }
}
