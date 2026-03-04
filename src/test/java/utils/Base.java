package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.EnvValidator;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

public class Base {
    protected static ExtentReports extent;

    // ThreadLocal — each thread (class) keeps its own ExtentTest references
    protected static final ThreadLocal<ExtentTest> classLevelTest = new ThreadLocal<>();
    protected static final ThreadLocal<ExtentTest> testLevelTest  = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        EnvValidator.validate();
        extent = ReportManager.getInstance();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        String className = this.getClass().getSimpleName();
        ExtentTest classTest;
        // createTest() mutates shared ExtentReports state — must be synchronized
        synchronized (ReportManager.class) {
            classTest = extent.createTest(className);
        }
        classLevelTest.set(classTest);
        System.out.println("\n=== Starting Test Class: " + className + " ===");
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method, ITestResult result) {
        String testName = method.getName();
        ExtentTest node;
        // createNode() also mutates shared state — must be synchronized
        synchronized (ReportManager.class) {
            node = classLevelTest.get().createNode(testName);
        }
        testLevelTest.set(node);
        System.out.println(">>> Starting Test: " + testName);
        node.info("Starting Test: " + testName);
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
