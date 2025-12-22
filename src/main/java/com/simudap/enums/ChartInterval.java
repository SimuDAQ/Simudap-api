package com.simudap.enums;

import com.simudap.error.ResourceNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChartInterval {
    MIN(null),
    DAY("D"),
    WEEK("W"),
    MONTH("M"),
    YEAR("Y"),
    ;

    private final String value;

    public static ChartInterval from(String interval) {
        return Arrays.stream(ChartInterval.values())
                .filter(value -> value.name().equalsIgnoreCase(interval))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found interval " + interval));
    }
}
