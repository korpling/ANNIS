package annis.gui.query_references;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class UrlShortenerEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column
  private String owner;
  @Column
  private Date created;

  @Column(nullable = false, unique = true)
  @Lob
  private URI url;
  @Column
  @Lob
  private URI temporaryUrl;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public Date getCreated() {
    return created;
  }

  public URI getTemporaryUrl() {
    return temporaryUrl;
  }

  public URI getUrl() {
    return url;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setUrl(URI url) {
    this.url = url;
  }

  public void setTemporaryUrl(URI temporaryUrl) {
    this.temporaryUrl = temporaryUrl;
  }
}
