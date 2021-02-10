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
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
import org.corpus_tools.annis.gui.security.SecurityConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class ReferenceLinkEditor extends Panel {

  private static final long serialVersionUID = 6191359393713574090L;
  private final Grid<UrlShortenerEntry> grid;
  private final TextField txtFilterId;
  private ConfigurableFilterDataProvider<UrlShortenerEntry, Void, UUID> dataProvider;

  public ReferenceLinkEditor() {
    grid = new Grid<>();
    grid.setSizeFull();

    Binder<UrlShortenerEntry> binder = grid.getEditor().getBinder();

    Column<UrlShortenerEntry, UUID> idColumn = grid.addColumn(UrlShortenerEntry::getId);
    idColumn.setCaption("UUID");
    idColumn.setSortProperty("id");

    Column<UrlShortenerEntry, Date> createdColumn = grid.addColumn(UrlShortenerEntry::getCreated);
    createdColumn.setCaption("Timestamp");
    createdColumn.setSortProperty("created");

    Column<UrlShortenerEntry, String> ownerColumn = grid.addColumn(UrlShortenerEntry::getOwner);
    ownerColumn.setCaption("Created by");
    ownerColumn.setSortProperty("owner");

    Column<UrlShortenerEntry, URI> temporaryColumn =
        grid.addColumn(UrlShortenerEntry::getTemporaryUrl);
    temporaryColumn.setCaption("Temporary URL");
    temporaryColumn.setSortProperty("temporaryUrl");
    TextField txtTemporary = new TextField();
    Binding<UrlShortenerEntry, String> temporaryBinding = binder.bind(txtTemporary, entry -> {
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
    temporaryColumn.setSortable(true);

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
          // Don't set the filter but ignore otherwisse
        }
      }
    });

    filterRow.getCell(idColumn).setComponent(txtFilterId);

    grid.getEditor().setEnabled(true);
    grid.getEditor().setBuffered(true);


    setSizeFull();
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

  @Override
  public void attach() {
    super.attach();

    if (getUI() instanceof AnnisUI) {
      AnnisUI annisUI = (AnnisUI) getUI();

      UrlShortener shortener = annisUI.getUrlShortener();
      DataProvider<UrlShortenerEntry, UUID> dp = DataProvider.fromFilteringCallbacks(query -> {
        Sort sort = Sort.by(query.getSortOrders().stream().map(o -> {

          if (o.getDirection() == SortDirection.DESCENDING) {
            return Order.desc(o.getSorted());
          } else {
            return Order.asc(o.getSorted());
          }

        })
            .collect(Collectors.toList()));
        PageRequest request = createPageRequest(query.getOffset(), query.getLimit(), sort);

        if (query.getFilter().isPresent()) {
          List<UrlShortenerEntry> result = new LinkedList<>();
          // TODO: how to get partial matches?
          try {
            Optional<UrlShortenerEntry> entry =
                shortener.getRepo().findById(query.getFilter().get());
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
      dataProvider = dp.withConfigurableFilter();
      grid.setDataProvider(dataProvider);

    }

    Optional<OidcUser> user = Helper.getUser(getUI());
    if (user.isPresent() && user.get().containsClaim(SecurityConfiguration.ROLES_CLAIM)
        && user.get().getClaimAsStringList(SecurityConfiguration.ROLES_CLAIM)
        .contains("admin")) {
      setContent(grid);
    }

  }

}
