package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.corpus_tools.annis.gui.TestHelper.awaitCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.mvysny.kaributesting.mockhttp.MockRequest;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.github.mvysny.kaributesting.v8.MockVaadinKt;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import org.corpus_tools.annis.gui.EmbeddedVisUI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
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
    void regression509() throws Exception {
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

        awaitCondition(30,
            () -> !_find(Link.class, spec -> spec.withCaption("Show in ANNIS search interface"))
                .isEmpty());
        Link link = _get(Link.class, spec -> spec.withCaption("Show in ANNIS search interface"));
        assertEquals("dontprint", link.getStyleName());
    }

    @Test
    void showRawText() throws Exception {
      EmbeddedVisUI ui = (EmbeddedVisUI) UI.getCurrent();

      MockRequest request = MockVaadinKt.getMock(VaadinRequest.getCurrent());
      request.setParameter("embedded_match", "pcc2/11299#tok_1");
      request.setParameter("embedded_fulltext", "");
      ui.attachToPath("/embeddedvis/raw_text", VaadinRequest.getCurrent());
      
      awaitCondition(60, () -> !_find(Label.class,
          spec -> spec.withPredicate(l -> "raw_text_label".equals(l.getStyleName()))).isEmpty());
      Label labelRawText = _get(Label.class,
          spec -> spec.withPredicate(l -> "raw_text_label".equals(l.getStyleName())));
      assertTrue(labelRawText.getValue()
          .startsWith("Feigenblatt Die Jugendlichen in Zossen wollen ein Musikcafé ."));
      assertTrue(labelRawText.getValue().endsWith("Die glänzten diesmal noch mit Abwesenheit ."));

    }

    /**
     * Make sure there is an error message when an unknown visualizer is requested.
     * 
     * @throws Exception
     */
    @Test
    void unknownRemoteSaltVisualizer() throws Exception {
      EmbeddedVisUI ui = (EmbeddedVisUI) UI.getCurrent();

      MockRequest request = MockVaadinKt.getMock(VaadinRequest.getCurrent());
      request.setParameter("embedded_salt", "http://example.com/does-not-exist.salt");
      ui.attachToPath("/embeddedvis/notavisualizer", VaadinRequest.getCurrent());

      
      awaitCondition(60,
          () -> !_find(Label.class,
              spec -> spec.withPredicate(
                  l -> l.getContentMode() == ContentMode.HTML && l.getValue().startsWith("<h1>")))
                      .isEmpty());
      Label labelMessage = _get(Label.class);
      assertEquals(
          "<h1>Unknown visualizer \"notavisualizer\"</h1><div>This ANNIS instance does not know the given visualizer.</div>",
          labelMessage.getValue());
    }

    @Test
    void invalidRemoteSaltUrlScheme() throws Exception {
      EmbeddedVisUI ui = (EmbeddedVisUI) UI.getCurrent();

      MockRequest request = MockVaadinKt.getMock(VaadinRequest.getCurrent());
      request.setParameter("embedded_salt", "file://example.com/does-not-exist.salt");
      ui.attachToPath("/embeddedvis/raw_text", VaadinRequest.getCurrent());


      awaitCondition(60,
          () -> !_find(Label.class,
              spec -> spec.withPredicate(
                  l -> l.getContentMode() == ContentMode.HTML && l.getValue().startsWith("<h1>")))
                      .isEmpty());
      Label labelMessage = _get(Label.class);
      assertEquals(
          "<h1>Could not generate the visualization.</h1><div>Expected URL scheme 'http' or 'https' but was 'file'</div>",
          labelMessage.getValue());
    }

}
