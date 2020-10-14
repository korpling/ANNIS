package annis.gui;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import org.slf4j.LoggerFactory;

// NOSONAR We want the awaitCondition code to be as near as possible to the Karibu async example
public class TestHelper {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TestHelper.class);

  private TestHelper() {
    // Static helper class
  }

  public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator)
      throws Exception {
    long startTime = System.currentTimeMillis();
    long maxExecutionTime = ((long) seconds) * 1000l;
    boolean condition = false;
    int attempt = 0;

    while (!condition && (System.currentTimeMillis() - startTime) < maxExecutionTime) {
      MockVaadin.INSTANCE.clientRoundtrip();

      log.debug("Evaluating await condition (attempt {})", attempt++);
      condition = conditionEvaluator.call();

      if (!condition) {
        // Wait until invoking the condition again
        log.debug("Waiting 250ms before checking condition again");
        Thread.sleep(250);
      }
    }

    MockVaadin.INSTANCE.clientRoundtrip();

    if (!condition) {
      throw new TimeoutException("Condition did not become true in " + seconds + " seconds.");
    }
  }
}
