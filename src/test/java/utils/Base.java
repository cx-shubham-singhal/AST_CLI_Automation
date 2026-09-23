package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.EnvValidator;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class Base {
    protected static ExtentReports extent;
    // Shared, not thread-local: under parallel="methods" the @BeforeClass thread
    // and the threads running that class's @Test methods can differ, so the class
    // node must be visible across threads rather than pinned to whichever thread
    // happened to run @BeforeClass.
    private static final ConcurrentHashMap<String, ExtentTest> classNodes = new ConcurrentHashMap<>();
    protected static ThreadLocal<ExtentTest> testLevelTest = new ThreadLocal<>();

    @BeforeSuite
    public void setupSuite() {
        EnvValidator.validate();
        extent = ReportManager.getInstance();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        String className = this.getClass().getSimpleName();
        classNodes.computeIfAbsent(className, extent::createTest);
        System.out.println("\n=== Starting Test Class: " + className + " ===");
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method, ITestResult result) {

        String testName = method.getName();
        String className = this.getClass().getSimpleName();

        ExtentTest classNode = classNodes.get(className);
        ExtentTest test = classNode.createNode(testName);
        testLevelTest.set(test);

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
