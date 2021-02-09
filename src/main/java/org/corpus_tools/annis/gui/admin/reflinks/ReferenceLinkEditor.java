package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderRow;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
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
    grid = new Grid<>();
    grid.setSizeFull();
    
    Binder<UrlShortenerEntry> binder = grid.getEditor().getBinder();

    Column<UrlShortenerEntry, UUID> idColumn = grid.addColumn(UrlShortenerEntry::getId);
    idColumn.setCaption("UUID");
    TextField txtUUID = new TextField();
    Binding<UrlShortenerEntry, String> idBinding =
        binder.bind(txtUUID, entry -> entry.getId().toString()
            , (entry, value) -> {
              entry.setId(UUID.fromString(value));
    });
    idColumn.setEditorBinding(idBinding);
    Column<UrlShortenerEntry, Date> createdColumn = grid.addColumn(UrlShortenerEntry::getCreated);
    createdColumn.setCaption("Timestamp");
    Column<UrlShortenerEntry, String> ownerColumn = grid.addColumn(UrlShortenerEntry::getOwner);
    ownerColumn.setCaption("Created by");
    Column<UrlShortenerEntry, URI> temporaryColumn =
        grid.addColumn(UrlShortenerEntry::getTemporaryUrl);
    temporaryColumn.setCaption("Temporary URL");
    Column<UrlShortenerEntry, URI> urlColumn = grid.addColumn(UrlShortenerEntry::getUrl);
    urlColumn.setCaption("URL");
    
    HeaderRow filterRow = grid.appendHeaderRow();
    
    txtFilterId = new TextField();
    txtFilterId.setPlaceholder("Filter by UUID");
    txtFilterId.setWidthFull();
    txtFilterId.addValueChangeListener((e) -> {
      refreshItems();
    });

    filterRow.getCell(idColumn).setComponent(txtFilterId);

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
