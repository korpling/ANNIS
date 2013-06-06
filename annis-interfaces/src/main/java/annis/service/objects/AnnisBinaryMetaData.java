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
public class AnnisBinaryMetaData implements Serializable
{

  private String localFileName;
  private String corpusName;
  private String mimeType;
  private String fileName;
  private int length;

  public String getCorpusName()
  {
    return corpusName;
  }

  public void setCorpusName(String corpusName)
  {
    this.corpusName = corpusName;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public int getLength()
  {
    return length;
  }

  public void setLength(int length)
  {
    this.length = length;
  }

  @XmlTransient
  public String getLocalFileName()
  {
    return localFileName;
  }

  public void setLocalFileName(String localFileName)
  {
    this.localFileName = localFileName;
  }
}
