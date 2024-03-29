package org.corpus_tools.annis.gui;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import org.slf4j.LoggerFactory;


public class TestHelper {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TestHelper.class);

  private TestHelper() {
    // Static helper class
  }

  public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator,
      Callable<String> message, int sleepTimeInMs)
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
        log.debug("Waiting {} ms before checking condition again", sleepTimeInMs);
        Thread.sleep(sleepTimeInMs); // NOSONAR The code should similar to the Karibu async example
      }
    }

    MockVaadin.INSTANCE.clientRoundtrip();

    if (!condition) {
      throw new TimeoutException(message.call());
    }
  }

  public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator,
      Callable<String> message) throws Exception {
    awaitCondition(seconds, conditionEvaluator, message, 250);
  }

  public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator)
      throws Exception {
    awaitCondition(seconds, conditionEvaluator,
        () -> "Condition did not become true in " + seconds + " seconds.", 250);
  }

  public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator,
      int sleepTimeInMs) throws Exception {
    awaitCondition(seconds, conditionEvaluator,
        () -> "Condition did not become true in " + seconds + " seconds.", sleepTimeInMs);
  }
}
