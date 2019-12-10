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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import annis.service.objects.AnnisCorpus;
import de.hu_berlin.german.korpling.annis.kickstarter.KickstartRunner;

/**
 *
 * @author thomas
 */
public class AcceptanceTest {
	private static final Logger log = LoggerFactory.getLogger(AcceptanceTest.class);

	private static KickstartRunner runner;
	private static WebDriver driver;

	private WebDriverWait wait;

	private final static int WEB_PORT = 8086;
	private final static int SERVICE_PORT = 5722;

	private static final Set<String> corpora = new LinkedHashSet<>();

	@BeforeClass
	public static void runKickstarter() {
		try {
			runner = new KickstartRunner(WEB_PORT, SERVICE_PORT, null);

			runner.startService();
			runner.startJetty();

			// get all installed corpora
			for (AnnisCorpus c : runner.getCorpora()) {
				corpora.add(c.getName());
			}

			FirefoxOptions opts = new FirefoxOptions();
			opts.setCapability("takesScreenshot", true);
			opts.setHeadless(true);
			driver = new FirefoxDriver(opts);
			driver.manage().window().setSize(new Dimension(1024, 768));

		} catch (Exception ex) {
			log.error("Could not create Firefox driver", ex);
			runner = null;
		}
	}

	@Before
	public void setup() {
		Assume.assumeNotNull(driver);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, 60);

		driver.get("http://localhost:" + WEB_PORT + "/annis-gui/");

		wait.until(driver -> driver.findElement(By.className("v-app")).isDisplayed());
	}

	protected void takeScreenshot(File outputFile) {
		if (driver instanceof TakesScreenshot) {
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			try {
				Files.copy(screenshot, outputFile);
			} catch (IOException ex) {
				log.error("Could not create screenshot", ex);
			}
		}
	}

	@Test
	public void testAboutWindow() {
		driver.findElement(By.id("SearchView:MainToolbar:btAboutAnnis")).click();
		Assert.assertTrue(driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).isDisplayed());
		driver.findElement(By.id("AboutWindow:VerticalLayout:btClose")).click();
	}

	@Test
	public void testOpenSourceWindow() {
		driver.findElement(By.id("SearchView:MainToolbar:btOpenSource")).click();
		Assert.assertTrue(driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).isDisplayed());
		driver.findElement(By.id("HelpUsWindow:VerticalLayout:btClose")).click();
	}

	@Test
	public void testTokenSearchPcc2() throws IOException {
		JavascriptExecutor js = (JavascriptExecutor) driver;


		// only execute this test if pcc2 corpus is imported
		Assert.assertTrue(corpora.contains("pcc2"));

		// execute a "tok" search on pcc2
		WebElement codeMirror = driver.findElement(
				By.xpath("//div[@id='SearchView:ControlPanel:QueryPanel']//div[contains(@class,'CodeMirror')]"));

		// activate the code mirror field (so we can leave it later)
		codeMirror.click();
		// set text by javascript
		js.executeScript("arguments[0].CodeMirror.setValue('tok');", codeMirror);

		// filter pcc2 corpus via text field to ensure it is visible in the table
		WebElement filterInput = driver
				.findElement(By.id("SearchView:ControlPanel:TabSheet:CorpusListPanel:txtFilter"));
		filterInput.click();
		filterInput.sendKeys("pcc2");

		By pccTrXPath = By.xpath("//div[@id='SearchView:ControlPanel:TabSheet:CorpusListPanel:tblCorpora']"
				+ "//table[contains(@class, 'v-table-table')]//tr//div[contains(text(), 'pcc2')]");

		wait.until(driver -> driver.findElement(pccTrXPath).isDisplayed());
		
		driver.findElement(pccTrXPath).click();

		driver.findElement(By.id("SearchView:ControlPanel:QueryPanel:btShowResult")).click();

		// wait until the result is loaded
		By byGridTable = By.xpath(
				"//div[@id='SearchView:TabSheet:ResultViewPanel:Panel:resultLayout:SingleResultPanel.1']/div[2]//table");
		wait.until(driver -> driver.findElement(byGridTable).isDisplayed());

		WebElement gridTable = driver.findElement(byGridTable);
		List<WebElement> firstRow = gridTable.findElements(By.xpath(".//tr[1]/td"));
		Assert.assertEquals(7, firstRow.size());
		Assert.assertEquals("Feigenblatt", firstRow.get(0).getText());
		Assert.assertEquals("Die", firstRow.get(1).getText());
		Assert.assertEquals("Jugendlichen", firstRow.get(2).getText());
		Assert.assertEquals("in", firstRow.get(3).getText());
		Assert.assertEquals("Zossen", firstRow.get(4).getText());
		Assert.assertEquals("wollen", firstRow.get(5).getText());
		Assert.assertEquals("ein", firstRow.get(6).getText());
	}

	/**
	 * Make sure the "Show in ANNIS search interface" link is shown in embedded
	 * visualizer. Regression test for https://github.com/korpling/ANNIS/issues/509
	 * (Link from embedded visualization to search UI is gone in 3.4.0)
	 */
	@Test
	public void testRegression509() {
		// only execute this test if pcc2 corpus is imported
		Assume.assumeTrue(corpora.contains("pcc2"));

		driver.get("http://localhost:" + WEB_PORT
				+ "/annis-gui/embeddedvis/grid?embedded_ns=exmaralda&embedded_instance=&embedded_salt=http%3A%2F%2Flocalhost%3A"
				+ SERVICE_PORT
				+ "%2Fannis%2Fquery%2Fsearch%2Fsubgraph%3Fmatch%3Dsalt%3A%2Fpcc2%2F11299%2F%2523tok_1%26left%3D5%26right%3D5&embedded_interface=http://localhost:8084/annis-gui/%23_q%3DdG9r%26_c%3DcGNjMg%26cl%3D5%26cr%3D5%26s%3D0%26l%3D10%26m%3D0");

		// wait until page was loaded
		wait.until(driver -> driver.findElement(By.className("v-app")).isDisplayed());
		// wait until visualization is actually there
		wait.until(driver -> driver.findElements(By.className("v-app-loading")).size() == 0);

		WebElement link = driver.findElement(By.xpath("//div[@id='VerticalLayout:Link']/a/span[2]"));
		Assert.assertEquals("Show in ANNIS search interface", link.getText());

	}

	@AfterClass
	public static void cleanup() {
		if (driver != null) {
			driver.quit();
		}
	}

}
