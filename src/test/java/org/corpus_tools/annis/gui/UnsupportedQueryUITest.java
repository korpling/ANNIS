package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.server.Page;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class UnsupportedQueryUITest {

  private static final String TARGET_URL = "http://localhost:8080?q=somequery";
  @Autowired
  private BeanFactory beanFactory;

  UnsupportedQueryUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(UnsupportedQueryUI.class);

    // Make sure we can spy on the page object of the UI
    Page page = spy(ui.getPage());
    ui.overwrittenPage = page;

    MockVaadin.setup(() -> ui);

  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void testExecuteQueryAnyway() {

    ui.getPanel().setUrl(TARGET_URL);

    _click(_get(Button.class,
        spec -> spec.withPredicate(btn -> btn.getCaption().startsWith("I understand the risks"))));
    // This should redirect us to the target URL
    verify(ui.getPage()).setLocation(eq(TARGET_URL));
  }

}
