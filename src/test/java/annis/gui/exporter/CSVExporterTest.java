package annis.gui.exporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.google.common.eventbus.EventBus;
import com.vaadin.spring.internal.UIScopeImpl;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test"})
@WebAppConfiguration
class CSVExporterTest {

  @Autowired
  private BeanFactory beanFactory;

  @Autowired
  private CSVExporter exporter;
  private AnnisUI ui;


  @BeforeEach
  void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);
    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void exportFeigenblattQuery() throws IOException {
    EventBus eventBus = mock(EventBus.class);
    Writer out = new StringWriter();

    Exception ex = exporter.convertText("tok=\"Feigenblatt\"", QueryLanguage.AQL, 5, 5,
        Sets.newSet("pcc2"), null, "", false, out, eventBus, new HashMap<>(), ui);

    assertNull(ex);

    // Compare the generated CSV file with the ground truth
    out.close();
    String[] lines = out.toString().split("\n");
    assertEquals(3, lines.length);
    assertEquals("1_id\t1_span\t1_anno_tiger::lemma\t1_anno_tiger::morph\t1_anno_tiger::pos",
        lines[0]);
    assertEquals("salt:/pcc2/11299#tok_1\tFeigenblatt\tFeigenblatt\tNom.Sg.Neut\tNN", lines[1]);
    assertEquals("salt:/pcc2/11299#tok_143\tFeigenblatt\tFeigenblatt\tDat.Sg.Neut\tNN", lines[2]);
  }

}
