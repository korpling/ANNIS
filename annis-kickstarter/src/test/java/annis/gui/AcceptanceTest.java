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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author thomas
 */
public class AcceptanceTest
{
  
  @Before
  public void setup()
  {
    
  }
  
  @Test
  public void testSimple() throws Exception
  {
    // Create a new instance of the Firefox driver
    // Notice that the remainder of the code relies on the interface, 
    // not the implementation.
    WebDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_38);

    // And now use this to visit NetBeans
    driver.get("http://www.netbeans.org");
    // Alternatively the same thing can be done like this
    // driver.navigate().to("http://www.netbeans.org");

    // Check the title of the page
    // Wait for the page to load, timeout after 10 seconds
    (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>()
    {
      @Override
      public Boolean apply(WebDriver d)
      {
        return d.getTitle().contains("NetBeans");
      }
    });

    //Close the browser
    driver.quit();
  }
  
}
