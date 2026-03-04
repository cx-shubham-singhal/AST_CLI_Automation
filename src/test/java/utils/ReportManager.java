package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportManager {

    // volatile ensures the fully-constructed object is visible to all threads
    private static volatile ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            synchronized (ReportManager.class) {
                if (extent == null) { // double-checked locking — safe for parallel threads
                    createReport();
                }
            }
        }
        return extent;
    }

    private static void createReport() {
        String reportDir = "test-output";
        cleanOldReports(reportDir);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String reportPath = reportDir + "/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("Checkmarx CLI Test Report");
        sparkReporter.config().setReportName("CXOne CLI Automation Results");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);

        extent.setSystemInfo("Tester", System.getProperty("user.name"));
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
    }

    private static void cleanOldReports(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith("ExtentReport_") && name.endsWith(".html"));
            if (files != null) {
                for (File file : files) {
                    try {
                        Files.delete(file.toPath());
                    } catch (Exception e) {
                        System.err.println("Failed to delete " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        } else {
            if (!dir.mkdirs()) {
                System.err.println("Warning: could not create report directory: " + dirPath);
            }
        }
    }
}
