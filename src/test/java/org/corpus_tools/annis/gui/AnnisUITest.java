package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mvysny.kaributesting.v8.GridKt;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel;
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
class AnnisUITest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);
   

    MockVaadin.setup(() -> ui);

    CorpusSet testCorpusSet = new CorpusSet();
    testCorpusSet.setName("test");
    testCorpusSet.getCorpora().add("pcc2");
    ui.getInstanceConfig().getCorpusSets().add(testCorpusSet);
    
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void testTitleFromInstanceConfig() {
    assertEquals("ANNIS", ui.getInstanceConfig().getInstanceDisplayName());
  }

  @Test
  void testChangeCorpusSet() {
    CorpusListPanel corpusListPanel = _get(CorpusListPanel.class);
    
    @SuppressWarnings("unchecked")
    Grid<String> corpusList = _get(corpusListPanel, Grid.class);
    
    @SuppressWarnings("unchecked")
    ComboBox<String> corpusSetChooser = _get(corpusListPanel, ComboBox.class);
    assertEquals(null, corpusSetChooser.getValue());


    assertTrue(GridKt._size(corpusList) > 1);

    _setValue(corpusSetChooser, "test");

    assertEquals(1, GridKt._size(corpusList));
    assertEquals("pcc2", GridKt._get(corpusList, 0));
  }

}
