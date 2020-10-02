package annis.gui.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseIntegrationTest {
    protected WebDriver driver;
    protected JavascriptExecutor js;
    protected WebDriverWait wait;


    @BeforeEach
    public void setUp() {
        driver = new FirefoxDriver();
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, 30);
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    protected void waitForUserInterfaceLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".v-ui")));
    }
}
