package org.corpus_tools.annis.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.corpus_tools.annis.gui.exporter.CSVExporter;
import org.corpus_tools.annis.gui.exporter.TextColumnExporter;
import org.corpus_tools.annis.gui.objects.ExportQuery;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.junit.jupiter.api.Test;

class QueryControllerTest {

  /**
   * Check that exporter name is only updated if actually changed.
   */
  @Test
  void testQueryExporterName() {
    AnnisUI ui = mock(AnnisUI.class);
    SearchView view = mock(SearchView.class);
    QueryUIState state = spy(new QueryUIState());
    when(ui.getQueryState()).thenReturn(state);

    QueryController controller = new QueryController(ui, view, state);
    ExportQuery currentQuery = controller.getExportQuery();
    assertEquals(CSVExporter.class, currentQuery.getExporter());

    // No setter function of the state should have been called if we set the same query again
    controller.setQuery(currentQuery);
    verify(state, never()).setExporter(any());

    // Modify the selected exporter and check that the setter has been called
    currentQuery.setExporter(TextColumnExporter.class);
    controller.setQuery(currentQuery);
    verify(state).setExporter(eq(TextColumnExporter.class));
  }

}
