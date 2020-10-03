package annis.gui.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;


public class RegressionTestsIT extends BaseIntegrationTest {

  @Test
  public void regression509() {
    driver.get(
        "http://localhost:5712/embeddedvis/grid?embedded_ns=exmaralda&embedded_instance=&embedded_match=pcc2/11299%23tok_1&embedded_left=5&embedded_right=5&embedded_interface=http://localhost:5712/%23_q%3DdG9r%26ql%3Daql%26_c%3DcGNjMg%26cl%3D5%26cr%3D5%26s%3D0%26l%3D10%26m%3D0");

    waitForUserInterfaceLoaded();

    assertThat(
        driver.findElement(By.xpath("//div[@id=\'VerticalLayout:Link\']/a/span[2]")).getText(),
        is("Show in ANNIS search interface"));
  }
}
