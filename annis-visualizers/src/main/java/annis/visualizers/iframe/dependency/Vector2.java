/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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
package annis.visualizers.iframe.dependency;

import annis.model.AnnisNode;

public class Vector2
{

  private long x;
  private long y;

  public Vector2(AnnisNode n)
  {
    this.x = n.getLeftToken();
    this.y = n.getRightToken();
  }

  public Vector2(long x, long y)
  {
    this.x = x;
    this.y = y;
  }

  public long getX()
  {
    return x;
  }

  public void setX(long x)
  {
    this.x = x;
  }

  public long getY()
  {
    return y;
  }

  public void setY(long y)
  {
    this.y = y;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final Vector2 other = (Vector2) obj;
    if (this.x != other.x)
    {
      return false;
    }
    if (this.y != other.y)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 83 * hash + (int) (this.x ^ (this.x >>> 32));
    hash = 83 * hash + (int) (this.y ^ (this.y >>> 32));
    return hash;
  }
}
