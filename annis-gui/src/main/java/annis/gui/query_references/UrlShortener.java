package annis.gui.query_references;

import annis.libgui.Helper;
import com.vaadin.ui.UI;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UrlShortener {

  @Autowired
  private UrlShortenerRepository repo;

  @Transactional
  public String shortenURL(URI original, UI ui) {
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
    List<UrlShortenerEntry> existingEntry = repo.findByUrl(localURL);
    if (existingEntry.isEmpty()) {

      UrlShortenerEntry entry = new UrlShortenerEntry();
      entry.setUrl(localURL);
      Optional<String> userName = Helper.getUserName(Helper.getToken());
      if (userName.isPresent()) {
        entry.setOwner(userName.get());
      } else {
        entry.setOwner("anonymous");
      }
      entry.setCreated(new Date());

      UrlShortenerEntry savedEntry = repo.save(entry);
      // The UUID should be generated when the entry is save
      shortID = savedEntry.getId();
    } else {
      shortID = existingEntry.get(0).getId();
    }
    return UriBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("").fragment("")
        .queryParam("id", shortID.toString()).build().toASCIIString();
  }

  public Optional<URI> unshorten(UUID id) {
    return repo.findById(id).map(e -> {
      if(e.getTemporaryUrl() == null) {
        return e.getUrl();
      } else {
        return e.getTemporaryUrl();
      }
    });
  }
}
