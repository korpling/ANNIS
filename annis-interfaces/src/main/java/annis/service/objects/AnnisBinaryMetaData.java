package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Provides the meta data of a binary file.
 *
 * @author Benjamin Weißenfels <p.pixeldrama@gmail.com>
 */
@XmlRootElement
public class AnnisBinaryMetaData implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6461460937985010477L;
  private String localFileName;
  private String mimeType;
  private String fileName;
  private int length;

  public String getFileName() {
    return fileName;
  }

  public int getLength() {
    return length;
  }

  @XmlTransient
  public String getLocalFileName() {
    return localFileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public void setLocalFileName(String localFileName) {
    this.localFileName = localFileName;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
