/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache based on temporary files. All files will deleted when this object
 * is finalized so be sure to hold a reference on it as long as you need it.
 */
public class FilesystemCache implements Cache
{

  private String namespace;
  private Map<String, File> fileMap;

  public FilesystemCache(String namespace) throws CacheInitializationException
  {
    this.fileMap = new HashMap<String, File>();
    this.namespace = namespace;
  }

  private File getFile(String key) throws IOException
  {
    File file;
    if ((file = fileMap.get(key)) != null)
    {
      return file;
    }
    file = File.createTempFile("anniscache_" + this.namespace + "_", key + ".cache");
    System.out.println("using file: " + file.getAbsolutePath());
    file.deleteOnExit();
    fileMap.put(key, file);
    return file;
  }

  /* (non-Javadoc)
   * @see annis.cache.Cache#get(java.lang.String)
   */
  public String get(String key) throws CacheException
  {
    try
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getFile(key)), "UTF-8"));
      String line;
      StringBuffer sBuffer = new StringBuffer();
      while ((line = in.readLine()) != null)
      {
        sBuffer.append(line + "\n");
      }
      return sBuffer.toString();
    } catch (IOException e)
    {
      throw new CacheException(e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see annis.cache.Cache#put(java.lang.String, java.lang.String)
   */
  public void put(String key, String value)
  {
    this.put(key, value.getBytes());
  }

  /* (non-Javadoc)
   * @see annis.cache.Cache#invalidateAll()
   */
  public void invalidateAll()
  {
    throw new RuntimeException("Not implemented");
  }

  /* (non-Javadoc)
   * @see annis.cache.Cache#invalidate()
   */
  public void invalidate()
  {
    throw new RuntimeException("Not implemented");
  }

  public byte[] getBytes(String key) throws CacheException
  {
    try
    {
      File file = getFile(key);
      long length = file.length();

      FileInputStream fis = new FileInputStream(file);

      byte[] bytes = new byte[(int) length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0)
      {
        offset += numRead;
      }
      //Ensure all the bytes have been read in
      if (offset < bytes.length)
      {
        throw new IOException("Could not completely read file " + file.getName());
      }

//	        while(offset < chars.length && (numRead = in.read(chars)) >= 0) {
//	        	sBuffer.append(chars);
//	        }   
      fis.close();
      return bytes;
    } catch (IOException e)
    {
      throw new CacheException("Cache miss on key: " + key + " (namespace: " + namespace + ") -> " + e.getMessage());
    } catch (NegativeArraySizeException e)
    {
      throw new CacheException("Cached file too large: " + key + " (namespace: " + namespace + ") -> " + e.getMessage());
    }
  }

  public void put(String key, byte[] value)
  {
    try
    {
      File file = getFile(key);
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(value);
      fos.flush();
      fos.close();
    } catch (IOException e)
    {
      //ignore
      e.printStackTrace();
    }
  }

  @Override
  protected void finalize() throws Throwable
  {
    super.finalize();

    for (File f : fileMap.values())
    {
      try
      {
        if (f.exists() && f.canWrite())
        {
          f.delete();
        }
      } catch (Exception ex)
      {
      }
    }
  }
}
