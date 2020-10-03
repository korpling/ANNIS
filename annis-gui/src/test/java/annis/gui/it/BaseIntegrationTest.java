package annis.gui.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void setUp() {
        driver.get("http://localhost:5712/");
        waitForUserInterfaceLoaded();
    }

    protected void waitForUserInterfaceLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".v-ui")));
    }

    protected void selectCorpus(String corpusName) {
        // Filter for the corpus name in case the corpus list has too many entries and does not it
        // show yet
        driver.findElement(By.id("SearchView-ControlPanel-TabSheet-CorpusListPanel-txtFilter"))
                .click();
        driver.findElement(By.id("SearchView-ControlPanel-TabSheet-CorpusListPanel-txtFilter"))
                .sendKeys(corpusName);

        // Explicitly select the corpus by clicking it in the list
        driver.findElement(By.xpath(
                "//div[@id=\'SearchView-ControlPanel-TabSheet-CorpusListPanel-tblCorpora\']/div[3]/table/tbody/tr/td/span/input"))
                .click();
    }
}
