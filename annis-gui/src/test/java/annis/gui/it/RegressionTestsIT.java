package annis.gui.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RegressionTestsIT {
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
  public void regression509() {
    driver.manage().window().setSize(new Dimension(1024, 768));
    driver.get(
        "http://localhost:5712/embeddedvis/grid?embedded_ns=exmaralda&embedded_instance=&embedded_match=pcc2/11299%23tok_1&embedded_left=5&embedded_right=5&embedded_interface=http://localhost:5712/%23_q%3DdG9r%26ql%3Daql%26_c%3DcGNjMg%26cl%3D5%26cr%3D5%26s%3D0%26l%3D10%26m%3D0");
    assertThat(
        driver.findElement(By.xpath("//div[@id=\'VerticalLayout:Link\']/a/span[2]")).getText(),
        is("Show in ANNIS search interface"));
  }
}
