package com.myorg.cxone.helpers;

import com.aventstack.extentreports.ExtentTest;

public class Logger {
    public static void info(String message, ExtentTest test) {
        if (message.contains("\n")) {
            // Wrap multi-line messages (like CLI output) in <pre> for better formatting
            test.info("<pre>" + escapeHtml(message) + "</pre>");
        } else {
            test.info(message);
        }
        System.out.println(message); // Also log to console
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static void pass(String message, ExtentTest test) {
        test.pass(message);           // Logs pass status + message to Extent report
        System.out.println("[PASS] " + message);
    }

    public static void fail(String message, ExtentTest test) {
        test.fail(message);           // Logs fail status + message to Extent report
        System.err.println("[FAIL] " + message);
    }
}