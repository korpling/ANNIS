/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen;

/**
 *
 * @author benjamin
 */
public class LimitOffsetQueryData
{

  private int offset;
  private int limit;

  public LimitOffsetQueryData(int offset, int limit)
  {
    this.offset = offset;
    this.limit = limit;
  }

  public int getLimit()
  {
    return limit;
  }

  public int getOffset()
  {
    return offset;
  }
  
  public boolean isPaged()
  {
    return offset != 0 || limit != 0;
  }
}
