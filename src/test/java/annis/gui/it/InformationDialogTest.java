package annis.gui.it;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.mvysny.kaributesting.v8.LocatorJ.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;

@SpringBootTest
@WebAppConfiguration
public class InformationDialogTest {

    @Autowired
    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
        MockVaadin.setup(() -> beanFactory.getBean(AnnisUI.class));
    }

    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    public void aboutWindow() throws InterruptedException {
        UI.getCurrent().getNavigator().navigateTo("");

        _click(_get(Button.class, spec -> spec.withCaption("About ANNIS")));

        // Check that the windows has opened
        assertNotNull(_get(Window.class, spec -> spec.withCaption("About ANNIS")));

        // Close the window again
        Button btClose = _get(Button.class, spec -> spec.withCaption("Close"));

        assertNotNull(btClose);
        _click(btClose);

        // Window should be closed
        assertEquals(0, _find(Window.class, spec -> spec.withCaption("About ANNIS")).size());
    }

}
