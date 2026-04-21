package com.example.tooltestingdemo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateUtil {
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        formatter = formatter == null ? DEFAULT_TIME_FORMATTER : formatter;
        return dateTime == null ? "" : formatter.format(dateTime);
    }
}
