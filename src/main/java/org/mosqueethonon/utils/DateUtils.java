package org.mosqueethonon.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateUtils {

    public static boolean isOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        long overlap = Math.min(end1.toEpochDay(), end2.toEpochDay()) -
                Math.max(start1.toEpochDay(), start2.toEpochDay());

        return overlap >= 0;
    }

    public static LocalDate fromStringToLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

}
