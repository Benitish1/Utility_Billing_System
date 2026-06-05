package com.wasac.billing.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public final class AppUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AppUtils() {
    }

    public static String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    public static String generateBillNumber(int year, int month, long sequence) {
        return String.format("BILL-%d%02d-%06d", year, month, sequence);
    }

    public static String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + RANDOM.nextInt(1000, 9999);
    }

    public static String buildNotificationMessage(String customerName, int month, int year, String amount) {
        String monthName = java.time.Month.of(month)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return String.format(
                "Dear %s,\nYour %s/%d utility bill of %s FRW has been successfully processed.",
                customerName, monthName, year, amount);
    }

    public static boolean isOtpExpired(LocalDateTime expiry) {
        return expiry == null || LocalDateTime.now().isAfter(expiry);
    }
}
