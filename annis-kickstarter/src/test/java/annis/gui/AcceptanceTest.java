/*
 * Copyright 2016 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.service.objects.AnnisCorpus;
import de.hu_berlin.german.korpling.annis.kickstarter.KickstartRunner;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.LinkedHashSet;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 *
 * @author thomas
 */
public class AcceptanceTest
{
  private static final Logger log = LoggerFactory.getLogger(AcceptanceTest.class);
  
  private static KickstartRunner runner;
  private static WebDriver driver;
  
  private WebDriverWait wait;
  
  private final static int WEB_PORT = 8086;
  private final static int SERVICE_PORT = 5722;
  
  private static final Set<String> corpora = new LinkedHashSet<>();
  
  @BeforeClass
  public static void runKickstarter()
  {
    try
    {
      runner = new KickstartRunner(WEB_PORT, SERVICE_PORT);
      
      runner.startService();
      runner.startJetty();
      
      // get all installed corpora
      for(AnnisCorpus c : runner.getCorpora())
      {
        corpora.add(c.getName());
      }
      
      driver = new FirefoxDriver();
    }
    catch (Exception ex)
    {
      log.error(null, ex);
      runner = null;
    }
  }
  
  @Before
  public void setup()
  {
    Assume.assumeNotNull(driver);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    wait = new WebDriverWait(driver, 10);
    
    driver.get("http://localhost:" + WEB_PORT +  "/annis-gui/");
    
    // initial wait for the title (can be longer than implicit wait time)
    wait.until(ExpectedConditions.titleContains("(ANNIS Corpus Search)"));
  }
  
  @Test
  public void testAboutWindow()
  {
    driver.findElement(By.id("MainToolbar:btAboutAnnis")).click();
    Assert.assertTrue(driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).isDisplayed());
    driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).click();
  }
  
  @Test
  public void testOpenSourceWindow()
  {
    driver.findElement(By.id("MainToolbar:btOpenSource")).click();
    Assert.assertTrue(driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).isDisplayed());
    driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).click();    
  }
  
  @Test
  public void testTokenSearchPcc2()
  {
    // only execute this test if pcc2 corpus is imported
    Assume.assumeTrue(corpora.contains("pcc2"));
    
    // execute a "tok" search on pcc2
    WebElement codeMirror = driver.findElement(By.xpath("//div[@id='SearchView:ControlPanel:QueryPanel']//div[contains(@class,'CodeMirror')]"));
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("arguments[0].CodeMirror.setValue('tok');", codeMirror);
 
    WebElement tdPcc = driver.findElement(By.xpath("//div[@id='SearchView:ControlPanel:TabSheet:CorpusListPanel:tblCorpora']//table[contains(@class, 'v-table-table')]//tr[1]/td[1]/div"));
    Assert.assertNotNull(tdPcc);
    Assert.assertEquals("pcc2", tdPcc.getText());
    tdPcc.click();
    
    driver.findElement(By.id("SearchView:ControlPanel:QueryPanel:btShowResult")).click();
    
  }
  
  @AfterClass
  public static void cleanup()
  {
    if(driver != null)
    {
      driver.quit();
    }
  }
  
}
