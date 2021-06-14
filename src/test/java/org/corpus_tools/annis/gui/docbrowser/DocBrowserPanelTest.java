package org.corpus_tools.annis.gui.docbrowser;

import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.corpus_tools.annis.gui.TestHelper.awaitCondition;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
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
class DocBrowserPanelTest {

  @Autowired
  private BeanFactory beanFactory;

  AnnisUI ui;


  @BeforeEach
  public void setup() throws Exception {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);

    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  private void openDocBrowser(String corpusName) throws Exception {
    ui.getSearchView().getDocBrowserController().openDocBrowser(corpusName);
    // Panel should be immediately loaded
    DocBrowserPanel panel = _get(DocBrowserPanel.class);
    assertNotNull(panel);

    // Wait for the actual document table to appear
    awaitCondition(30, () -> {
      return !_find(DocBrowserTable.class).isEmpty();
    }, () -> "Document browser table did not appear");

  }

  @Test
  void testCorpusPathListed() throws Exception {

    openDocBrowser("pcc2");

    DocBrowserTable table = _get(DocBrowserTable.class);
    Collection<?> ids = table.getItemIds();
    assertEquals(2, ids.size());

    assertTrue(ids.contains("pcc2 > 11299"));
    assertTrue(ids.contains("pcc2 > 4282"));
  }

  @Test
  void testDocumentOrder() throws Exception {
    openDocBrowser("shenoute.a22");

    DocBrowserTable table = _get(DocBrowserTable.class);

    // Test order of entries, which is explicitly given in the document_browser.json of the corpus
    // (Ordering by title, ascending)
    List<String> ids =
        table.getItemIds().stream().map(o -> o.toString()).collect(Collectors.toList());

    assertEquals(4, ids.size());

    assertEquals("shenoute.a22 > a22.YA421-428", ids.get(0));
    assertEquals("shenoute.a22 > a22.YA517-518", ids.get(1));
    assertEquals("shenoute.a22 > a22.YB307-320", ids.get(2));
    assertEquals("shenoute.a22 > a22.ZC301-308", ids.get(3));
  }

  @Test
  void testMetadata() throws Exception {
    openDocBrowser("shenoute.a22");

    DocBrowserTable table = _get(DocBrowserTable.class);

    assertEquals("MONB.YA", table.getContainerProperty("shenoute.a22 > a22.YA421-428", "msName"));
    assertEquals("MONB.YA", table.getContainerProperty("shenoute.a22 > a22.YA517-518", "msName"));

    assertEquals("MONB.YB", table.getContainerProperty("shenoute.a22 > a22.YB307-320", "msName"));
    assertEquals("MONB.ZC", table.getContainerProperty("shenoute.a22 > a22.ZC301-308", "msName"));
  }

}
