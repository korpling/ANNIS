/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.libgui.visualizers;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Hold the data and meta information for a resource needed by an iframe
 * based visualizer.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class IFrameResource implements Serializable
{
  private byte[] data = new byte[0];
  private String mimeType;

  public byte[] getData()
  {
    return Arrays.copyOf(data, data.length);
  }

  public void setData(byte[] data)
  {
    if(data == null)
    {
      this.data = new byte[0];
    }
    else
    {
      this.data = Arrays.copyOf(data, data.length);
    }
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }
  
  
}
