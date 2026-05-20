package com.vyapaarbuddy.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date/time operations.
 * TODO: Implement date formatting for Indian locale
 * TODO: Add date range validation methods
 * TODO: Add business day calculation
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static String formatDate(LocalDate date) {
        // TODO: Implement date formatting
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        // TODO: Implement date-time formatting
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateString) throws DateTimeParseException {
        // TODO: Implement date parsing
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    public static boolean isToday(LocalDate date) {
        // TODO: Implement today check
        return date.equals(LocalDate.now());
    }

    public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement date range check
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public static LocalDate getStartOfMonth(LocalDate date) {
        // TODO: Implement start of month calculation
        return date.withDayOfMonth(1);
    }

    public static LocalDate getEndOfMonth(LocalDate date) {
        // TODO: Implement end of month calculation
        return date.withDayOfMonth(date.lengthOfMonth());
    }
}
