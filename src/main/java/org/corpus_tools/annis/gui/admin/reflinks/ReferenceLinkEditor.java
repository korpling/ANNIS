package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderRow;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class ReferenceLinkEditor extends Panel {

  private static final long serialVersionUID = 6191359393713574090L;
  private final Grid<UrlShortenerEntry> grid;
  private final TextField txtFilterId;
  private ConfigurableFilterDataProvider<UrlShortenerEntry, Void, UUID> dataProvider;
  private Column<UrlShortenerEntry, URI> temporaryColumn;

  public ReferenceLinkEditor() {
    grid = new Grid<>();
    grid.setSizeFull();

    Column<UrlShortenerEntry, UUID> idColumn = grid.addColumn(UrlShortenerEntry::getId);
    idColumn.setCaption("UUID");
    idColumn.setSortProperty("id");

    Column<UrlShortenerEntry, Date> createdColumn = grid.addColumn(UrlShortenerEntry::getCreated);
    createdColumn.setCaption("Timestamp");
    createdColumn.setSortProperty("created");

    Column<UrlShortenerEntry, String> ownerColumn = grid.addColumn(UrlShortenerEntry::getOwner);
    ownerColumn.setCaption("Created by");
    ownerColumn.setSortProperty("owner");

    temporaryColumn = grid.addColumn(UrlShortenerEntry::getTemporaryUrl);
    temporaryColumn.setCaption("Temporary URL");
    temporaryColumn.setSortProperty("temporaryUrl");
    temporaryColumn.setId("temporaryUrl");

    Column<UrlShortenerEntry, URI> urlColumn = grid.addColumn(UrlShortenerEntry::getUrl);
    urlColumn.setCaption("URL");
    urlColumn.setSortProperty("url");

    HeaderRow filterRow = grid.appendHeaderRow();

    txtFilterId = new TextField();
    txtFilterId.setPlaceholder("Find UUID");
    txtFilterId.setWidthFull();
    txtFilterId.addValueChangeListener(event -> {
      dataProvider.setFilter(null);
      if (event.getValue() != null && !event.getValue().isEmpty()) {
        try {
          UUID id = UUID.fromString(event.getValue());
          dataProvider.setFilter(id);
        } catch (IllegalArgumentException ex) {
          // Don't set the filter but ignore otherwise
        }
      }
    });

    filterRow.getCell(idColumn).setComponent(txtFilterId);


    grid.getEditor().setEnabled(true);
    grid.getEditor().setBuffered(true);


    setSizeFull();
  }

  private void addEditableBindings(Column<UrlShortenerEntry, URI> temporaryColumn,
      DataProvider<UrlShortenerEntry, ?> provider, AnnisUI ui) {
    TextField txtTemporary = new TextField();
    Binder<UrlShortenerEntry> binder = grid.getEditor().getBinder();

    Binding<UrlShortenerEntry, String> temporaryBinding = binder.bind(txtTemporary,
        new TemporaryUrlValueProvider(), new TemporaryUrlSetter(ui, provider));
    temporaryColumn.setEditorBinding(temporaryBinding);

  }

  private PageRequest createPageRequest(int offset, int limit, Sort sort) {
    int minPageSize = limit;
    int lastIndex = offset + limit - 1;
    int maxPageSize = lastIndex + 1;

    for (double pageSize = minPageSize; pageSize <= maxPageSize; pageSize++) {
      int startPage = (int) (offset / pageSize);
      int endPage = (int) (lastIndex / pageSize);
      if (startPage == endPage) {
        return PageRequest.of(startPage, (int) pageSize, sort);
      }
    }
    return PageRequest.of(0, maxPageSize, sort);
  }

  private DataProvider<UrlShortenerEntry, UUID> createDataProvider(UrlShortener shortener) {
    return DataProvider.fromFilteringCallbacks(query -> {
      Sort sort = Sort.by(query.getSortOrders().stream().map(o -> {

        if (o.getDirection() == SortDirection.DESCENDING) {
          return Order.desc(o.getSorted());
        } else {
          return Order.asc(o.getSorted());
        }

      }).collect(Collectors.toList()));
      PageRequest request = createPageRequest(query.getOffset(), query.getLimit(), sort);

      if (query.getFilter().isPresent()) {
        List<UrlShortenerEntry> result = new LinkedList<>();
        try {
          Optional<UrlShortenerEntry> entry = shortener.getRepo().findById(query.getFilter().get());
          if (entry.isPresent()) {
            result.add(entry.get());
          }
        } catch (IllegalArgumentException ex) {
          // Ignore
        }
        return result.stream();

      } else {
        return shortener.getRepo().findAll(request).stream();

      }
    }, query -> {
      if (query.getFilter().isPresent()) {
        if (shortener.getRepo().findById(query.getFilter().get()).isPresent()) {
          return 1;
        } else {
          return 0;
        }
      } else {
        return (int) shortener.getRepo().count();
      }
    });
  }

  @Override
  public void attach() {
    super.attach();

    if (getUI() instanceof AnnisUI) {
      AnnisUI annisUI = (AnnisUI) getUI();

      UrlShortener shortener = annisUI.getUrlShortener();


      dataProvider = createDataProvider(shortener).withConfigurableFilter();
      grid.setDataProvider(dataProvider);
      addEditableBindings(temporaryColumn, dataProvider, annisUI);

    }

    Optional<OAuth2User> user = Helper.getUser();
    if (user.isPresent()) {
      Set<String> roles = Helper.getUserRoles(user.get());
      if (roles.contains("admin")) {
        setContent(grid);
      }
    }
  }

}
