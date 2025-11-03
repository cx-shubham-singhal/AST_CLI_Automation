package utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class ExcelReportListener implements IReporter {

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Results");

        int rowNum = 0;

        // Header
        Row header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("Class Name");
        header.createCell(1).setCellValue("Test Case Name");
        header.createCell(2).setCellValue("Status");
        header.createCell(3).setCellValue("CLI Command"); // ✅ CLI Command column

        // Collect results grouped by class name
        Map<String, List<ITestResult>> resultsByClass = new TreeMap<>();

        for (ISuite suite : suites) {
            for (ISuiteResult result : suite.getResults().values()) {
                ITestContext context = result.getTestContext();

                collectResults(resultsByClass, context.getPassedTests());
                collectResults(resultsByClass, context.getFailedTests());
                collectResults(resultsByClass, context.getSkippedTests());
            }
        }

        // Write results class by class
        for (Map.Entry<String, List<ITestResult>> entry : resultsByClass.entrySet()) {
            for (ITestResult testResult : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(testResult.getMethod().getMethodName());
                row.createCell(2).setCellValue(
                        testResult.getStatus() == ITestResult.SUCCESS ? "PASS" :
                                testResult.getStatus() == ITestResult.FAILURE ? "FAIL" : "SKIPPED"
                );

                // Add CLI command (from attribute)
                Object cliCmd = testResult.getAttribute("cliCommand");
                row.createCell(3).setCellValue(cliCmd != null ? cliCmd.toString() : "N/A");
            }
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);

        // Write to file
        try {
            File file = new File(outputDirectory + "/TestReport.xlsx");
            if (file.exists()) file.delete();

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            System.out.println("✅ Excel report created at: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void collectResults(Map<String, List<ITestResult>> resultsByClass, IResultMap tests) {
        for (ITestResult result : tests.getAllResults()) {
            String className = result.getTestClass().getRealClass().getSimpleName();
            resultsByClass.putIfAbsent(className, new ArrayList<>());
            resultsByClass.get(className).add(result);
        }
    }
}
