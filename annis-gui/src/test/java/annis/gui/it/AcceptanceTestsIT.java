package annis.gui.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AcceptanceTestsIT extends BaseIntegrationTest {

  private static final String CELL_FEIGENBLATT =
      "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[1]";

  @Test
  public void aboutWindow() {
    driver.get("http://localhost:5712/");
    waitForUserInterfaceLoaded();
    driver.findElement(By.id("SearchView:MainToolbar:btAboutAnnis")).click();
    List<WebElement> elements = driver.findElements(By.id("AboutWindow:VerticalLayout:btClose"));
    assertTrue(elements.size() > 0);

    driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).click();
  }

  @Test
  public void openSourceWindow() {
    driver.get("http://localhost:5712/");
    waitForUserInterfaceLoaded();
    driver.findElement(By.id("SearchView:MainToolbar:btOpenSource")).click();

    List<WebElement> elements = driver.findElements(By.id("HelpUsWindow:VerticalLayout:btClose"));
    assertTrue(elements.size() > 0);

    driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).click();
  }

  @Test
  public void tokenSearchPcc2() {
    driver.get("http://localhost:5712/");
    waitForUserInterfaceLoaded();

    // Filter for the corpus name in case the corpus list has too many entries and does not show the
    // pcc2 corpus yet
    driver.findElement(By.id("SearchView:ControlPanel:TabSheet:CorpusListPanel:txtFilter")).click();
    driver.findElement(By.id("SearchView:ControlPanel:TabSheet:CorpusListPanel:txtFilter"))
        .sendKeys("pcc2");

    // Explicitly select the corpus by clicking it in the list
    driver.findElement(By.xpath(
        "//div[@id=\'SearchView:ControlPanel:TabSheet:CorpusListPanel:tblCorpora\']/div[3]/table/tbody/tr/td/span/input"))
        .click();


    driver.findElement(By.cssSelector(".CodeMirror-line")).click();
    driver.findElement(By.cssSelector(".CodeMirror textarea")).sendKeys("tok");


    driver.findElement(By.id("SearchView:ControlPanel:QueryPanel:btShowResult")).click();
    
    // Wait for second result to appear
    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(CELL_FEIGENBLATT)));

    // Test that the cell values have the correct token value
    assertThat(driver.findElement(By.xpath(CELL_FEIGENBLATT)).getText(), is("Feigenblatt"));


    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[2]"))
        .getText(), is("Die"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id='SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[3]"))
        .getText(), is("Jugendlichen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[4]"))
        .getText(), is("in"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[5]"))
        .getText(), is("Zossen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[6]"))
        .getText(), is("wollen"));
    assertThat(driver.findElement(By.xpath(
        "//div[@id=\'SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]/div/div[2]/div/div/table/tbody/tr/td[7]"))
        .getText(), is("ein"));
  }
}
