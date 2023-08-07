package org.corpus_tools.annis.gui.query_references;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
      entry.setId(UUID.randomUUID());
      entry.setUrl(localURL);
      Optional<OAuth2User> user = Helper.getUser();
      if (user.isPresent()) {
        entry.setOwner(Helper.getDisplayName(user.get()));
      } else {
        entry.setOwner("anonymous");
      }
      entry.setCreated(new Date());

      UrlShortenerEntry savedEntry = repo.save(entry);
      shortID = savedEntry.getId();
    } else {
      shortID = existingEntry.get(0).getId();
    }
    return UriComponentsBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("")
        .queryParam("id", shortID.toString()).build().toUriString();
  }

  @Transactional
  public void migrate(URI url, URI temporary, String userName, UUID uuid, Date creationTime) {
    Optional<UrlShortenerEntry> existing = repo.findById(uuid);
    
    Preconditions.checkState(!existing.isPresent(),
        "Attempted to migrate UUID {} which already exists in the database.", uuid);

    UrlShortenerEntry entry = new UrlShortenerEntry();
    entry.setId(uuid);
    entry.setOwner(userName);
    entry.setCreated(creationTime);
    entry.setUrl(url);
    entry.setTemporaryUrl(temporary);
    repo.save(entry);
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

  public UrlShortenerRepository getRepo() {
    return repo;
  }
}
