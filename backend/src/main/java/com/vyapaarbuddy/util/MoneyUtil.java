package com.vyapaarbuddy.util;

import com.vyapaarbuddy.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MoneyUtil {

    private static final DecimalFormat INR_FORMAT =
            new DecimalFormat("₹#,##0.00", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal calculateBalance(BigDecimal total, BigDecimal paid) {
        return defaultZero(total).subtract(defaultZero(paid)).setScale(SCALE, ROUNDING);
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPositiveOrZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return defaultZero(a).add(defaultZero(b)).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return defaultZero(a).subtract(defaultZero(b)).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return defaultZero(a).multiply(defaultZero(b)).setScale(SCALE, ROUNDING);
    }

    public static void validateNonNegative(BigDecimal value, String fieldName) {
        if (isNegative(value)) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
    }

    public static void validatePositive(BigDecimal value, String fieldName) {
        if (!isPositive(value)) {
            throw new BadRequestException(fieldName + " must be greater than zero");
        }
    }

    public static String formatINR(BigDecimal amount) {
        return INR_FORMAT.format(defaultZero(amount));
    }

    public static BigDecimal roundToTwoDecimals(BigDecimal amount) {
        return defaultZero(amount).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        return defaultZero(amount)
                .multiply(defaultZero(percentage))
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING);
    }
}
