package annis.cache;

import java.io.IOException;

public interface Cache {

	public abstract String get(String key) throws CacheException;
	
	public abstract byte[] getBytes(String key) throws CacheException;

	public abstract void put(String key, String value);
	
	public abstract void put(String key, byte[] value);

	public abstract void invalidateAll();

	public abstract void invalidate();

}