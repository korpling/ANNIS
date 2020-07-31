package annis.gui.query_references;

import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import com.vaadin.ui.UI;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.UriBuilder;
import org.springframework.data.repository.CrudRepository;

public interface UrlShortenerRepository extends CrudRepository<UrlShortenerEntry, UUID> {

  public default String shortenURL(URI original, UI ui) {
    String appContext = Helper.getContext(ui);

    String path = original.getRawPath();
    if (path.startsWith(appContext)) {
      path = path.substring(appContext.length());
    }

    String localURL = path;
    if (original.getRawQuery() != null) {
      localURL = localURL + "?" + original.getRawQuery();
    }
    if (original.getRawFragment() != null) {
      localURL = localURL + "#" + original.getRawFragment();
    }

    UrlShortenerEntry entry = new UrlShortenerEntry();
    entry.setUrl(URI.create(localURL));
    AnnisUser user = Helper.getUser(ui);
    if (user == null) {
      entry.setOwner("anonymous");
    } else {
      entry.setOwner(user.getUserName());
    }
    entry.setCreated(new Date());
    
    UrlShortenerEntry savedEntry = this.save(entry);
    // The UUID should be generated when the entry is save
    UUID shortID = savedEntry.getId();

    return UriBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("").fragment("")
        .queryParam("id", shortID.toString()).build().toASCIIString();
  }
}
