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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import de.hu_berlin.german.korpling.annis.kickstarter.KickstartRunner;
import java.util.concurrent.TimeUnit;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class AcceptanceTest
{
  private Logger log = LoggerFactory.getLogger(AcceptanceTest.class);
  
  private KickstartRunner runner;
  private WebDriver driver;
  
  private WebDriverWait wait;
  
  public AcceptanceTest()
  {
    try
    {
      runner = new KickstartRunner();
      
      runner.startService();
      runner.startJetty();
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
    Assume.assumeNotNull(runner);
    //driver = new FirefoxDriver();
    HtmlUnitDriver htmlDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_38);
    htmlDriver.setJavascriptEnabled(true);
    htmlDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    driver = htmlDriver;
    wait = new WebDriverWait(driver, 10);
    
    driver.get("http://localhost:8080/annis-gui/");
    
    //wait.until(ExpectedConditions.titleContains("(ANNIS Corpus Search)"));
  }
  
  @Test
  public void testAboutWindow()
  { 
    driver.findElement(By.id("MainToolbar:btAboutAnnis")).click();
    driver.findElement(By.id("AnnisUI:AboutWindow")).isDisplayed();
  }
  
}
