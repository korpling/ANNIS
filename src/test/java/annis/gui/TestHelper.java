package annis.gui;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.github.mvysny.kaributesting.v8.MockVaadin;

import org.awaitility.Awaitility;

public class TestHelper {
    private TestHelper() {
        // Static helper class
    }

    public static void awaitCondition(int seconds, Callable<Boolean> conditionEvaluator) {
        MockVaadin.INSTANCE.runUIQueue(true);
        // Wait for the first result to appear
        Awaitility.pollInSameThread();
        await().atMost(seconds, TimeUnit.SECONDS).until(conditionEvaluator);
        MockVaadin.INSTANCE.runUIQueue(true);

    }
}
