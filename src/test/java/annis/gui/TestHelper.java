package annis.gui;

import static org.awaitility.Awaitility.await;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;

public class TestHelper {
    private TestHelper() {
        // Static helper class
    }

    public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator) {
      Awaitility.pollInSameThread();
      MockVaadin.INSTANCE.clientRoundtrip();
      await().atMost(seconds, TimeUnit.SECONDS).until(() -> {
        MockVaadin.INSTANCE.clientRoundtrip();
        Boolean result = conditionEvaluator.call();
        MockVaadin.INSTANCE.clientRoundtrip();
        return result;
      });
      MockVaadin.INSTANCE.clientRoundtrip();
    }
}
