package annis.gui.query_references;

import annis.gui.CommonUI;
import annis.libgui.Helper;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UrlShortener {

  @Autowired
  private UrlShortenerRepository repo;

  @Transactional
  public String shortenURL(URI original, CommonUI ui) {
    String appContext = ui.getServletContext().getContextPath();

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
      Optional<OidcUser> user = Helper.getUser(ui);
      if (user.isPresent()) {
        entry.setOwner(Helper.getDisplayName(user.get()));
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
    return UriComponentsBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("")
        .queryParam("id", shortID.toString()).build().toUriString();
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
