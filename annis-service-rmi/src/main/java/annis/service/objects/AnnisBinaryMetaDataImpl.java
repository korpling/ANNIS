package annis.service.objects;

import annis.service.ifaces.AnnisBinaryMetaData;

/**
 * This Class provides the Metadata of a BinaryFile.
 * @author benjamin
 */
public class AnnisBinaryMetaDataImpl implements AnnisBinaryMetaData
{

  protected static final long serialVersionUID = -4484371544441543151L;
  protected byte[] bytes;
  protected long id;
  protected String mimeType;
  protected String fileName;
  protected int length;

  @Override
  public long getId()
  {
    return this.id;
  }

  @Override
  public String getMimeType()
  {
    return this.mimeType;
  }

  @Override
  public String getFileName()
  {
    return this.fileName;
  }

  @Override
  public int getLength()
  {
    return this.length;
  }
}
