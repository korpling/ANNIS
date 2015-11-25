/*
 * Copyright 2015 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.security;

import java.io.Serializable;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

/**
 * A wrapper around {@link SimpleByteSource} to make it {@link Serializable}
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SerializableByteSource implements ByteSource, Serializable
{
  
  /**
   * The wrapped object.
   * Since {@link SimpleByteSource} is not serializable itself this field
   * is transient.
   * Therfore only access it with the {@link #getDelegate() } getter function.
   * 
   */
  private transient SimpleByteSource delegate;
  
  private final byte[] bytes;
  
  public SerializableByteSource()
  {
    this.bytes = new byte[0];
  }
  
  public SerializableByteSource(byte[] bytes)
  {
    this.bytes = bytes;
  }
  
  public SerializableByteSource(ByteSource source)
  {
    if(source == null)
    {
      this.bytes = new byte[0];
    }
    else
    {
      this.bytes = source.getBytes();
    }
  }

  public SimpleByteSource getDelegate()
  {
    if(delegate == null)
    {
      delegate = new SimpleByteSource(bytes);
    }
    return delegate;
  }

  @Override
  public byte[] getBytes()
  {
    return this.bytes;
  }

  @Override
  public String toHex()
  {
    return getDelegate().toHex();
  }

  @Override
  public String toBase64()
  {
    return getDelegate().toBase64();
  }

  @Override
  public boolean isEmpty()
  {
    return getDelegate().isEmpty();
  }

  
}
