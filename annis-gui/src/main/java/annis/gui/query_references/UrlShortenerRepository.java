package annis.gui.query_references;

import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import com.vaadin.ui.UI;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.UriBuilder;
import org.springframework.data.repository.CrudRepository;

public interface UrlShortenerRepository extends CrudRepository<UrlShortenerEntry, UUID> {

  public List<UrlShortenerEntry> findByUrl(URI url);

  public default String shortenURL(URI original, UI ui) {
    String appContext = Helper.getContext(ui);

    String path = original.getRawPath();
    if (path.startsWith(appContext)) {
      path = path.substring(appContext.length());
    }

    String localURLRaw = path;
    if (original.getRawQuery() != null) {
      localURLRaw = localURLRaw + "?" + original.getRawQuery();
    }
    if (original.getRawFragment() != null) {
      localURLRaw = localURLRaw + "#" + original.getRawFragment();
    }

    URI localURL = URI.create(localURLRaw);
    UUID shortID;
    // Check if this URI has already been shortened
    List<UrlShortenerEntry> existingEntry = this.findByUrl(localURL);
    if(existingEntry.isEmpty()) {
      
      UrlShortenerEntry entry = new UrlShortenerEntry();
      entry.setUrl(localURL);
      AnnisUser user = Helper.getUser(ui);
      if (user == null) {
        entry.setOwner("anonymous");
      } else {
        entry.setOwner(user.getUserName());
      }
      entry.setCreated(new Date());
      
      UrlShortenerEntry savedEntry = this.save(entry);
      // The UUID should be generated when the entry is save
      shortID = savedEntry.getId();
    } else {
      shortID = existingEntry.get(0).getId();
    }
    return UriBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("").fragment("")
        .queryParam("id", shortID.toString()).build().toASCIIString();
  }
}
