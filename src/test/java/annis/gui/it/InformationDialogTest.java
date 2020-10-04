package annis.gui.it;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles("desktop")
@WebAppConfiguration
class InformationDialogTest {

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
    void aboutWindow() throws InterruptedException {
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

    @Test
    void openSourceWindow() {
        UI.getCurrent().getNavigator().navigateTo("");

        _click(_get(Button.class, spec -> spec.withCaption("Help us make ANNIS better!")));

        // Check that the windows has opened
        assertNotNull(_get(Window.class, spec -> spec.withCaption("Help us make ANNIS better!")));

        // Close the window again
        Button btClose = _get(Button.class, spec -> spec.withCaption("Close"));
        assertNotNull(btClose);
        _click(btClose);

        // Window should be closed
        assertEquals(0,
                _find(Window.class, spec -> spec.withCaption("Help us make ANNIS better!")).size());
    }
}
