package annis.gui.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseIntegrationTest {
    protected static WebDriver driver;
    protected static WebDriverWait wait;


    @BeforeAll
    public static void setUpClass() {
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 10);
        driver.manage().window().setSize(new Dimension(1024, 768));
    }

    @AfterAll
    public static void tearDownClass() {
        driver.quit();
    }

    protected void waitForUserInterfaceLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".v-ui")));
    }
}
