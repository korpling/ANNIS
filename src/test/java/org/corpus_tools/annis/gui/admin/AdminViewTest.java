package org.corpus_tools.annis.gui.admin;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.TabSheet;
import java.io.IOException;
import net.jcip.annotations.NotThreadSafe;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
import org.corpus_tools.annis.gui.admin.reflinks.MigrationPanel;
import org.corpus_tools.annis.gui.admin.reflinks.ReferenceLinkEditor;
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
@NotThreadSafe
class AdminViewTest {


  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;



  @BeforeEach
  void setup() throws IOException {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);

    MockVaadin.setup(() -> ui);

    _click(_get(Button.class, spec -> spec.withCaption("Administration")));

  }

  @AfterEach
  public void tearDown() throws IOException {
    MockVaadin.tearDown();
  }


  @Test
  void testFragmentChanges() throws Exception {
    // Select all of the available tabs and check that the fragment is updated
    TabSheet tab = _get(TabSheet.class);

    tab.setSelectedTab(_get(ImportPanel.class));
    assertEquals("!admin/import", ui.getPage().getUriFragment());

    tab.setSelectedTab(_get(CorpusAdminPanel.class));
    assertEquals("!admin/corpora", ui.getPage().getUriFragment());

    tab.setSelectedTab(_get(ReferenceLinkEditor.class));
    assertEquals("!admin/reference-link-editor", ui.getPage().getUriFragment());

    tab.setSelectedTab(_get(MigrationPanel.class));
    assertEquals("!admin/reference-link-migration", ui.getPage().getUriFragment());
  }


}
