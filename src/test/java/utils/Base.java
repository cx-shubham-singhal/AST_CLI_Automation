package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.EnvValidator;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

public class Base {
    protected static ExtentReports extent;
    protected static ThreadLocal<ExtentTest> classLevelTest = new ThreadLocal<>();
    protected static ThreadLocal<ExtentTest> testLevelTest = new ThreadLocal<>();

    @BeforeSuite
    public void setupSuite() {
        EnvValidator.validate();
        extent = ReportManager.getInstance();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        String className = this.getClass().getSimpleName();
        classLevelTest.set(extent.createTest(className));
        System.out.println("\n=== Starting Test Class: " + className + " ===");
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method, ITestResult result) {
        String testName = method.getName();

        // Create node under class-level test
        ExtentTest test = classLevelTest.get().createNode(testName);
        testLevelTest.set(test);

        // Log to console + Extent report
        System.out.println(">>> Starting Test: " + testName);
        test.info("Starting Test: " + testName);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
            System.out.println("\n✅ Extent report flushed successfully after suite execution.");
        }
    }

    public ExtentTest getTestLogger() {
        return testLevelTest.get();
    }
}
