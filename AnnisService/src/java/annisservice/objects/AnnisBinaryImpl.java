package annisservice.objects;

import annisservice.ifaces.AnnisBinary;

public class AnnisBinaryImpl implements AnnisBinary {
	private byte[] bytes;
	private long id;
	private String mimeType;
	private String fileName;
	
	public byte[] getBytes() {
		return this.bytes;
	}

	public long getId() {
		return this.id;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getJSON() {
		return "{id: " + this.id + ", mimeType: '" + this.mimeType + ", bytes: '" + new String(this.bytes) + "'}";
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
