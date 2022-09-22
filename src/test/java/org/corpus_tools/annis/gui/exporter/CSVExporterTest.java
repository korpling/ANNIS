package org.corpus_tools.annis.gui.exporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.google.common.eventbus.EventBus;
import com.vaadin.spring.internal.UIScopeImpl;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.CorpusConfigurationView;
import org.corpus_tools.annis.api.model.CorpusConfigurationViewTimelineStrategy;
import org.corpus_tools.annis.api.model.CorpusConfigurationViewTimelineStrategy.StrategyEnum;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
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
@ActiveProfiles({"desktop", "test", "headless"})
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

  @Test
  void exportMetadata() throws IOException {
    EventBus eventBus = mock(EventBus.class);
    Writer out = new StringWriter();

    Exception ex = exporter.convertText("tok=\"Feigenblatt\"", QueryLanguage.AQL, 5, 5,
        Sets.newSet("pcc2"), null, "metakeys=Genre", false, out, eventBus, new HashMap<>(), ui);

    assertNull(ex);

    // Compare the generated CSV file with the ground truth
    out.close();
    String[] lines = out.toString().split("\n");
    assertEquals(3, lines.length);
    assertEquals(
        "1_id\t1_span\t1_anno_tiger::lemma\t1_anno_tiger::morph\t1_anno_tiger::pos\tmeta_Genre",
        lines[0]);
    assertEquals("salt:/pcc2/11299#tok_1\tFeigenblatt\tFeigenblatt\tNom.Sg.Neut\tNN\tPolitik",
        lines[1]);
    assertEquals("salt:/pcc2/11299#tok_143\tFeigenblatt\tFeigenblatt\tDat.Sg.Neut\tNN\tPolitik",
        lines[2]);
  }

  @Test
  void exportWithSegmentationMultipleToken() throws IOException {
    EventBus eventBus = mock(EventBus.class);
    Writer out = new StringWriter();


    Exception ex = exporter.convertText("utterance0=\"äh fang einfach ma an\"", QueryLanguage.AQL, 0, 0,
        Sets.newSet("dialog.demo"), null, "segmentation=phon0", false, out, eventBus,
        new HashMap<>(), ui);

    assertNull(ex);

    // Compare the generated CSV file with the ground truth
    out.close();
    String[] lines = out.toString().split("\n");
    assertEquals(2, lines.length);
    assertEquals("1_id\t1_span\t1_anno_default_ns::utterance0",
        lines[0]);
    assertEquals(
        "salt:/dialog.demo/dialog.demo#sSpan98\täh ((lacht)) fang einfach ma an\täh fang einfach ma an",
        lines[1]);
  }

  @Test
  void exportWithSegmentationSingleToken() throws IOException {
    EventBus eventBus = mock(EventBus.class);
    Writer out = new StringWriter();

    Exception ex =
        exporter.convertText("norm0=\"mal\"", QueryLanguage.AQL, 0, 0, Sets.newSet("dialog.demo"),
            null, "segmentation=phon0", false, out, eventBus, new HashMap<>(), ui);

    assertNull(ex);

    // Compare the generated CSV file with the ground truth
    out.close();
    String[] lines = out.toString().split("\n");
    assertEquals(2, lines.length);
    assertEquals("1_id\t1_span\t1_anno_default_ns::norm0", lines[0]);
    assertEquals("salt:/dialog.demo/dialog.demo#sSpan79\tma\tmal", lines[1]);
  }

  @Test
  void exportDialogWithTimelineConfiguration() throws IOException {
    EventBus eventBus = mock(EventBus.class);
    Writer out = new StringWriter();

    // Create a mapping and configuration for the dialog.demo corpus virtual tokenization
    HashMap<String, String> mappings = new HashMap<>();
    mappings.put("default_ns::utterance0", "phon0");
    mappings.put("default_ns::norm0", "phon0");
    mappings.put("default_ns::utterance1", "phon1");
    mappings.put("default_ns::norm1", "phon1");
    CorpusConfigurationViewTimelineStrategy timelineStrategy =
        new CorpusConfigurationViewTimelineStrategy();
    timelineStrategy.setStrategy(StrategyEnum.IMPLICITFROMMAPPING);
    timelineStrategy.setMappings(mappings);


    CorpusConfigurationView configView = new CorpusConfigurationView();
    configView.setTimelineStrategy(timelineStrategy);

    CorpusConfiguration singleConfig = new CorpusConfiguration();
    singleConfig.setView(configView);
    
    Map<String, CorpusConfiguration> allConfigs = new HashMap<>();
    allConfigs.put("dialog.demo", singleConfig);

    Exception ex = exporter.convertText("utterance1 _o_ phon1=\"so\"", QueryLanguage.AQL, 5, 5,
        Sets.newSet("dialog.demo"), null, "", false, out, eventBus, allConfigs, ui);

    assertNull(ex);

    // Compare the generated CSV file with the ground truth
    out.close();
    String[] lines = out.toString().split("\n");
    assertEquals(4, lines.length);
    // There is no annotation column for the second node, because it is converted into a token layer
    assertEquals("1_id\t1_span\t1_anno_default_ns::utterance1\t2_id\t2_span",
        lines[0]);
    assertEquals(
        "salt:/dialog.demo/dialog.demo#sSpan68\t"
            + "naja pass auf also du hast jetz wohl hier auch so ne Karte wie ich bloß ich hab ne Linie und du nich eine\t"
            + "naja pass auf also du hast jetzt wohl hier auch so ne Karte wie ich bloß ich hab ne Linie und du nich ne\t"
            + "salt:/dialog.demo/dialog.demo#sTok46\t" + "so",
        lines[1]);
    assertEquals(
        "salt:/dialog.demo/dialog.demo#sSpan70\t"
            + "und ich muss dir jetzt erklärn wie du vom Start zum Ziel kommst so wie meine Linie geht\t"
            + "und ich muss dir jetzt erklären wie du vom Start zum Ziel kommst so wie meine Linie geht\t"
            + "salt:/dialog.demo/dialog.demo#sTok46\t" + "so",
        lines[2]);
    assertEquals(
        "salt:/dialog.demo/dialog.demo#sSpan71\t" + "so\t" + "so\t"
            + "salt:/dialog.demo/dialog.demo#sTok14\t" + "so",
        lines[3]);
  }

}
