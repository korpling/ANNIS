package annis.service.ifaces;

import java.io.Serializable;

/**
 * This is the actual container for a single search result.
 * 
 * @author k.huetter
 *
 */
public interface AnnisBinary extends Serializable, JSONAble 
{

	public byte[] getBytes();
	public void setBytes(byte[] bytes);
	
	public long getId();
	public void setId(long id);
	
	public String getMimeType();
	public void setMimeType(String mimeType);
	
	public String getFileName();
	public void setFileName(String fileName);
	
}