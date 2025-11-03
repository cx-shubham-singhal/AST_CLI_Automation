package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.EnvValidator;
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

    @BeforeClass
    public void setupClass() {
        String className = this.getClass().getSimpleName();
        classLevelTest.set(extent.createTest(className));
    }

    @BeforeMethod
    public void setupTest(Method method) {
        String testName = method.getName();
        ExtentTest test = classLevelTest.get().createNode(testName);
        testLevelTest.set(test);
    }

    @AfterSuite
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
        }
    }

    public ExtentTest getTestLogger() {
        return testLevelTest.get();
    }
}
