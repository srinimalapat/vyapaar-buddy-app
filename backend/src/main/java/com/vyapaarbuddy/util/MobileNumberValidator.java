package com.vyapaarbuddy.util;

import java.util.regex.Pattern;

/**
 * Utility class for mobile number validation.
 * TODO: Implement Indian mobile number validation
 * TODO: Add international number support
 * TODO: Add phone number formatting
 */
public class MobileNumberValidator {

    // Indian mobile number pattern: +91 followed by 10 digits
    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^\\+91[6-9]\\d{9}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    public static boolean isValidIndianMobile(String mobile) {
        // TODO: Implement Indian mobile number validation
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        return INDIAN_MOBILE_PATTERN.matcher(mobile).matches() || MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static String formatIndianMobile(String mobile) {
        // TODO: Implement mobile number formatting
        if (mobile == null || mobile.isEmpty()) {
            return mobile;
        }
        if (mobile.startsWith("+91")) {
            return mobile;
        }
        if (mobile.startsWith("91") && mobile.length() == 12) {
            return "+" + mobile;
        }
        if (mobile.length() == 10) {
            return "+91" + mobile;
        }
        return mobile;
    }

    public static String normalizeMobile(String mobile) {
        // TODO: Implement mobile number normalization
        if (mobile == null) {
            return null;
        }
        return mobile.replaceAll("[^0-9+]", "");
    }
}
