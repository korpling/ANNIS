package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.data.provider.Sort;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderRow;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;

public class ReferenceLinkEditor extends Panel {

  private static final long serialVersionUID = 6191359393713574090L;
  private final Grid<UrlShortenerEntry> grid;
  private final TextField txtFilterId;
  private FilterablePageableDataProvider<UrlShortenerEntry, Object> dataProvider;

  public ReferenceLinkEditor() {
    grid = new Grid<>();
    grid.setSizeFull();
    
    Binder<UrlShortenerEntry> binder = grid.getEditor().getBinder();

    Column<UrlShortenerEntry, UUID> idColumn = grid.addColumn(UrlShortenerEntry::getId);
    idColumn.setCaption("UUID");

    Column<UrlShortenerEntry, Date> createdColumn = grid.addColumn(UrlShortenerEntry::getCreated);
    createdColumn.setCaption("Timestamp");

    Column<UrlShortenerEntry, String> ownerColumn = grid.addColumn(UrlShortenerEntry::getOwner);
    ownerColumn.setCaption("Created by");

    Column<UrlShortenerEntry, URI> temporaryColumn =
        grid.addColumn(UrlShortenerEntry::getTemporaryUrl);
    temporaryColumn.setCaption("Temporary URL");
    TextField txtTemporary = new TextField();
    Binding<UrlShortenerEntry, String> temporaryBinding =
        binder.bind(txtTemporary, entry -> {
          if (entry.getTemporaryUrl() == null) {
            return "";
          } else {
            return entry.getTemporaryUrl().toString();
          }
        }, (entry, value) -> {
          if (value == null || value.isEmpty()) {
            entry.setTemporaryUrl(null);
          } else {
            try {
              entry.setTemporaryUrl(new URI(value));
            } catch (URISyntaxException ex) {
              ExceptionDialog.show(ex, getUI());
            }
          }
          if (getUI() instanceof AnnisUI) {
            AnnisUI annisUI = (AnnisUI) getUI();
            UrlShortener shortener = annisUI.getUrlShortener();
            shortener.getRepo().save(entry);
          }

        });
    temporaryColumn.setEditorBinding(temporaryBinding);


    Column<UrlShortenerEntry, URI> urlColumn = grid.addColumn(UrlShortenerEntry::getUrl);
    urlColumn.setCaption("URL");
    
    HeaderRow filterRow = grid.appendHeaderRow();
    
    txtFilterId = new TextField();
    txtFilterId.setPlaceholder("Filter by UUID");
    txtFilterId.setWidthFull();
    txtFilterId.addValueChangeListener((e) -> {
      if (dataProvider != null) {
        dataProvider.refreshAll();
      }
    });

    filterRow.getCell(idColumn).setComponent(txtFilterId);

    grid.getEditor().setEnabled(true);
    grid.getEditor().setBuffered(true);



  }
  
  @Override
  public void attach() {
    super.attach();

    if (getUI() instanceof AnnisUI) {
      AnnisUI annisUI = (AnnisUI) getUI();
      UrlShortener shortener = annisUI.getUrlShortener();
      dataProvider = new FilterablePageableDataProvider<UrlShortenerEntry, Object>() {

        private static final long serialVersionUID = -1727729720680112512L;

        @Override
        protected Page<UrlShortenerEntry> fetchFromBackEnd(
            Query<UrlShortenerEntry, Object> query, Pageable pageable) {
          return shortener.getRepo().findAll(pageable);
        }

        @Override
        protected List<QuerySortOrder> getDefaultSortOrders() {
          return Sort.asc("id").build();
        }

        @Override
        protected int sizeInBackEnd(Query<UrlShortenerEntry, Object> query) {
          return (int) shortener.getRepo().count();
        }


      };
      grid.setDataProvider(dataProvider);
    }
    
    setContent(grid);
  }

}
