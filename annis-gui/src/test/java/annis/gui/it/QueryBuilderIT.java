package annis.gui.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

public class QueryBuilderIT extends BaseIntegrationTest {

    @Test
    public void flatQueryBuilder() {
        selectCorpus("pcc2");

        driver.findElement(By.cssSelector("#SearchView-ControlPanel-QueryPanel-btShowQueryBuilder"))
                .click();
        // Click on the arrow of the query builder selection combobox
        driver.findElement(By.cssSelector(
                "#SearchView-TabSheet-QueryBuilderChooser-VerticalLayout-cbChooseBuilder > .v-filterselect-button"))
                .click();
        // Select the "flat query builder"
        driver.findElement(By.cssSelector(
                ".v-filterselect-suggestmenu > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1)"))
                .click();
        // Initialize the query builder
        final By lingSequenceSelector = By.cssSelector(
                "#SearchView-TabSheet-QueryBuilderChooser-VerticalLayout-FlatQueryBuilder-VerticalLayout-language-btInitLanguage");
        assertEquals("Initialize", driver.findElement(lingSequenceSelector).getText());
        driver.findElement(lingSequenceSelector).click();

        // The button caption should have been changed
        final By lingSequenceLayout = By.cssSelector(
                "#SearchView-TabSheet-QueryBuilderChooser-VerticalLayout-FlatQueryBuilder-VerticalLayout div:nth-child(1)");
        assertEquals("Add", driver.findElement(lingSequenceLayout)
                .findElement(By.cssSelector(".v-menubar")).getText());

        // Add a single token by clicking on the "Add" button
        driver.findElement(lingSequenceLayout).findElement(By.cssSelector(".v-menubar")).click();
        driver.findElements(By.cssSelector(".popupContent .v-menubar-menuitem")).get(5).click();

        driver.findElement(By.cssSelector(
                "#SearchView-TabSheet-QueryBuilderChooser-VerticalLayout-FlatQueryBuilder-VerticalLayout-language input.v-filterselect-input"))
                .sendKeys("something");

        driver.findElement(By.cssSelector(
                "#SearchView-TabSheet-QueryBuilderChooser-VerticalLayout-FlatQueryBuilder-VerticalLayout-HorizontalLayout-btGo"))
                .click();;

        // The query should have been updated
        assertEquals("PP=/something/",
                driver.findElement(By.cssSelector(".CodeMirror-code pre")).getText());
    }
}
