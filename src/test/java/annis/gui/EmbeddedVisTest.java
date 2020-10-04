package annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.*;

import com.github.mvysny.kaributesting.mockhttp.MockRequest;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.github.mvysny.kaributesting.v8.MockVaadinKt;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import annis.SingletonBeanStoreRetrievalStrategy;

@SpringBootTest
@ActiveProfiles("desktop")
@WebAppConfiguration
class EmbeddedVisTest {

    @Autowired
    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
        MockVaadin.setup(() -> beanFactory.getBean(EmbeddedVisUI.class));

    }

    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void regression509() throws InterruptedException {
        EmbeddedVisUI ui = (EmbeddedVisUI) UI.getCurrent();
        MockRequest request = MockVaadinKt.getMock(VaadinRequest.getCurrent());
        request.setParameter("embedded_ns", "exmaralda");
        request.setParameter("embedded_instance", "");
        request.setParameter("embedded_match", "pcc2/11299#tok_1");
        request.setParameter("embedded_left", "5");
        request.setParameter("embedded_right", "5");
        request.setParameter("embedded_interface",
                "http://localhost:5712/#_q=dG9r&ql=aql&_c=cGNjMg&cl=5&cr=5&s=0&l=10&m=0");
        ui.attachToPath("/embeddedvis/grid", VaadinRequest.getCurrent());

        Awaitility.pollInSameThread();
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            MockVaadin.INSTANCE.runUIQueue(true);
            return !_find(Link.class, spec -> spec.withCaption("Show in ANNIS search interface"))
                    .isEmpty();

        });
        MockVaadin.INSTANCE.runUIQueue(true);

        Link link = _get(Link.class, spec -> spec.withCaption("Show in ANNIS search interface"));
        assertEquals("dontprint", link.getStyleName());
    }

}