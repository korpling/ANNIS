package org.corpus_tools.annis.gui.admin.reflinks;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mvysny.kaributesting.v8.GridKt;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import net.jcip.annotations.NotThreadSafe;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
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
class ReferenceLinkEditorTest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  private ReferenceLinkEditor panel;

  private UrlShortenerEntry entry2;

  private UrlShortenerEntry entry1;


  @BeforeEach
  void setup() throws IOException {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);

    MockVaadin.setup(() -> ui);

    _click(_get(Button.class, spec -> spec.withCaption("Administration")));
    TabSheet tab = _get(TabSheet.class);
    panel = _get(ReferenceLinkEditor.class);
    tab.setSelectedTab(panel);

    // Add some example entries
    ui.getUrlShortener().getRepo().deleteAll();
    UrlShortener urlShortener = this.ui.getUrlShortener();
    entry1 = new UrlShortenerEntry();
    entry1.setId(UUID.fromString("4366b0a5-6b27-40fe-ac5d-08e75c9eef51"));
    entry1.setUrl(URI.create("/test1"));
    urlShortener.getRepo().save(entry1);

    entry2 = new UrlShortenerEntry();
    entry2.setId(UUID.fromString("b1912b10-93f3-4018-84e8-6bf7572ee163"));
    entry2.setUrl(URI.create("/test2"));
    entry2.setTemporaryUrl(URI.create("/temp2"));
    urlShortener.getRepo().save(entry2);

  }

  @AfterEach
  void cleanup() {
    ui.getUrlShortener().getRepo().deleteAll();
  }



  @Test
  void testShowAndSortEntries() {
    @SuppressWarnings("unchecked")
    Grid<UrlShortenerEntry> grid = _get(panel, Grid.class);

    assertEquals(2, GridKt._size(grid));
    assertEquals(entry1, GridKt._get(grid, 0));
    assertEquals(entry2, GridKt._get(grid, 1));

    // Sort by the "Temporary URL" column
    grid.sort(grid.getColumns().get(3), SortDirection.ASCENDING);
    assertEquals(entry1, GridKt._get(grid, 0));
    assertEquals(entry2, GridKt._get(grid, 1));

    grid.sort(grid.getColumns().get(3), SortDirection.DESCENDING);
    assertEquals(entry2, GridKt._get(grid, 0));
    assertEquals(entry1, GridKt._get(grid, 1));
  }

  @Test
  void testFilterByUUID() {
    @SuppressWarnings("unchecked")
    Grid<UrlShortenerEntry> grid = _get(panel, Grid.class);

    TextField filter = _get(grid, TextField.class);

    // Set an existing UUID
    _setValue(filter, "4366b0a5-6b27-40fe-ac5d-08e75c9eef51");
    assertEquals(1, GridKt._size(grid));

    // Set an invalid UUID, this should not apply the filter
    _setValue(filter, "4366b0a5-");
    assertEquals(2, GridKt._size(grid));
    
    // Set to a non-existing but valid UUID, this should hide all entries
    _setValue(filter, "8307fbb6-f426-433d-9b11-71244b970e0d");
    assertEquals(0, GridKt._size(grid));
  }

  @Test
  void testManyEntriesAscending() {
    @SuppressWarnings("unchecked")
    Grid<UrlShortenerEntry> grid = _get(panel, Grid.class);

    grid.sort(grid.getColumns().get(4), SortDirection.ASCENDING);

    // Add random UUIDs
    for (int i = 0; i < 10000; i++) {
      UrlShortenerEntry e = new UrlShortenerEntry();
      e.setId(UUID.randomUUID());
      e.setUrl(URI.create("/doesnotexist"));
      ui.getUrlShortener().getRepo().save(e);
    }

    assertEquals(10002, GridKt._size(grid));

    // Add a single UUID which will be shown at the beginning of the grid when sorted
    UrlShortenerEntry firstEntry = new UrlShortenerEntry();
    firstEntry.setId(UUID.randomUUID());
    firstEntry.setUrl(URI.create("/"));
    ui.getUrlShortener().getRepo().save(firstEntry);

    assertEquals(firstEntry, GridKt._get(grid, 0));

  }

  @Test
  void testManyEntriesDescending() {
    @SuppressWarnings("unchecked")
    Grid<UrlShortenerEntry> grid = _get(panel, Grid.class);

    grid.sort(grid.getColumns().get(4), SortDirection.DESCENDING);

    // Add random UUIDs
    for (int i = 0; i < 10000; i++) {
      UrlShortenerEntry e = new UrlShortenerEntry();
      e.setId(UUID.randomUUID());
      e.setUrl(URI.create("/doesnotexist"));
      ui.getUrlShortener().getRepo().save(e);
    }

    assertEquals(10002, GridKt._size(grid));

    // Add a single UUID which will be shown at the end of the grid when sorted
    UrlShortenerEntry lastEntry = new UrlShortenerEntry();
    lastEntry.setId(UUID.randomUUID());
    lastEntry.setUrl(URI.create("/"));
    ui.getUrlShortener().getRepo().save(lastEntry);

    assertEquals(lastEntry, GridKt._get(grid, 10002));
  }

}
