package annis.gui.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AcceptanceTestsIT {
  private WebDriver driver;
  JavascriptExecutor js;

  @BeforeEach
  public void setUp() {
    driver = new FirefoxDriver();
    js = (JavascriptExecutor) driver;
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @AfterEach
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void aboutWindow() {
    driver.get("http://localhost:5712/");
    driver.manage().window().setSize(new Dimension(1024, 768));
    js.executeScript("window.scrollTo(0,0)");
    driver.findElement(By.id("SearchView:MainToolbar:btAboutAnnis")).click();
    {
      List<WebElement> elements = driver.findElements(By.id("AboutWindow:VerticalLayout:btClose"));
      assert (elements.size() > 0);
    }
    driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).click();
  }

  @Test
  public void openSourceWindow() {
    driver.get("http://localhost:5712/");
    driver.manage().window().setSize(new Dimension(1024, 768));
    driver.findElement(By.id("SearchView:MainToolbar:btOpenSource")).click();
    js.executeScript("window.scrollTo(0,0)");
    {
      List<WebElement> elements = driver.findElements(By.id("HelpUsWindow:VerticalLayout:btClose"));
      assert (elements.size() > 0);
    }
    driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).click();
  }

  @Test
  public void tokenSearchPcc2() {
    driver.get("http://localhost:5712/");
    driver.manage().window().setSize(new Dimension(1024, 768));
    js.executeScript("window.scrollTo(0,0)");
    driver.findElement(By.cssSelector(".CodeMirror-line")).click();
    driver.findElement(By.cssSelector(".CodeMirror textarea")).sendKeys("tok");
    driver.findElement(By.id("SearchView:ControlPanel:TabSheet:CorpusListPanel:txtFilter")).click();
    driver.findElement(By.id("SearchView:ControlPanel:TabSheet:CorpusListPanel:txtFilter"))
        .sendKeys("pcc2");
    driver.findElement(By.xpath(
        "//div[@id=\'SearchView:ControlPanel:TabSheet:CorpusListPanel:tblCorpora\']/div[3]/table/tbody/tr/td/span/input"))
        .click();
    driver.findElement(By.id("SearchView:ControlPanel:QueryPanel:btShowResult")).click();
    js.executeScript("window.scrollTo(0,0)");
    {
      WebDriverWait wait = new WebDriverWait(driver, 30);
      wait.until((driver) -> ExpectedConditions.presenceOfElementLocated(By.cssSelector(
          "//div[@id='SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel']/div[2]/div/div[2]/div/div/table/tbody/tr/td")));
    }
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td"))
        .getText(), is("Feigenblatt"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td[2]"))
        .getText(), is("Die"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id='SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel']/div[2]/div/div[2]/div/div/table/tbody/tr/td[3]"))
        .getText(), is("Jugendlichen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td[4]"))
        .getText(), is("in"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td[5]"))
        .getText(), is("Zossen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td[6]"))
        .getText(), is("wollen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1\']/div[2]/div/div[2]/div/div/table/tbody/tr/td[7]"))
        .getText(), is("ein"));
  }
}
