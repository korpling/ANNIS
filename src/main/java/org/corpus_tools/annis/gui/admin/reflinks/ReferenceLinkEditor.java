package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.ui.Grid;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderRow;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;

public class ReferenceLinkEditor extends Panel {

  private static final long serialVersionUID = 6191359393713574090L;
  private final Grid<UrlShortenerEntry> grid;
  private final TextField txtFilterId;

  public ReferenceLinkEditor() {
    grid = new Grid<>(UrlShortenerEntry.class);


    grid.setSizeFull();
    HeaderRow filterRow = grid.appendHeaderRow();
    
    txtFilterId = new TextField();
    txtFilterId.setPlaceholder("Filter by UUID");
    txtFilterId.setWidthFull();
    txtFilterId.addValueChangeListener((e) -> {
      refreshItems();
    });

    filterRow.getCell("id").setComponent(txtFilterId);

  }
  
  private void refreshItems() {
    if (getUI() instanceof AnnisUI) {
      AnnisUI annisUI = (AnnisUI) getUI();
      final UrlShortener shortener = annisUI.getUrlShortener();
      Stream<UrlShortenerEntry> items = shortener.list().stream();
      if(!txtFilterId.getValue().isEmpty()) {
        items = items.filter((item) -> item.getId().toString().contains(txtFilterId.getValue()));
      }
      grid.setItems(items.collect(Collectors.toList()));

    }
  }

  @Override
  public void attach() {
    super.attach();

    
    refreshItems();
    setContent(grid);
  }

}
