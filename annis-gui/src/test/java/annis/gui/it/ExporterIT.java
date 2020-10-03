package annis.gui.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ExporterIT extends BaseIntegrationTest {

    /**
     * Execute an example export and check that the download button gets visible at the end of the
     * process.
     */
    @Test
    public void exportDownloadButtonEnabled() {
        driver.get("http://localhost:5712/");
        waitForUserInterfaceLoaded();

        // Prepare query
        selectCorpus("pcc2");
        driver.findElement(By.cssSelector(".CodeMirror-line")).click();
        driver.findElement(By.cssSelector(".CodeMirror textarea")).sendKeys("tok=\"Feigenblatt\"");

        // Click on the "More" button and then "Export"
        driver.findElement(By.cssSelector("#SearchView-ControlPanel-QueryPanel-btMoreActions"))
                .click();
        driver.findElement(By.cssSelector(
                ".v-popupbutton-popup > div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)"))
                .click();

        // Make sure the Export tab is there
        assertTrue(driver
                .findElement(
                        By.cssSelector("#SearchView-TabSheet-ExportPanel-FormLayout-cbExporter"))
                .isDisplayed());

        // The download button should be disabled
        final By downloadButtonSelector =
                By.cssSelector("#SearchView-TabSheet-ExportPanel-HorizontalLayout-btDownload");
        assertTrue(driver.findElement(downloadButtonSelector).getAttribute("class")
                .contains("v-disabled"));

        // Click on "Perform Export" button, wait until export is finished and download button is
        // enabled
        driver.findElement(
                By.cssSelector("#SearchView-TabSheet-ExportPanel-HorizontalLayout-btExport"))
                .click();

        wait.until(driver -> !driver.findElement(downloadButtonSelector).getAttribute("class")
                .contains("v-disabled"));

    }
}
