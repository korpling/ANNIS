/*
* Copyright 2013 SFB 632.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package annis.visualizers.htmlvis;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
*
* @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
*/
public class OutputItem implements Comparable<OutputItem>
{
  private String qName;
  private long length;
  private String outputString;

  public String getqName()
  {
    return qName;
  }

  public void setqName(String qName)
  {
    this.qName = qName;
  }

  public long getLength()
  {
    return length;
  }

  public void setLength(long length)
  {
    this.length = length;
  }

  public String getOutputString()
  {
    return outputString;
  }

  public void setOutputString(String outputString)
  {
    this.outputString = outputString;
  }
  
  @Override
  public int compareTo(OutputItem o)
  {
    if(o == null)
    {
      throw new NullPointerException();
    }
    
    return ComparisonChain.start()
      .compare(o.getLength(), length)
      .compare(qName, o.getqName())
      .result();
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj != null && obj instanceof OutputItem)
    {
      return compareTo((OutputItem) obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(length, qName);
  }

  @Override
  public String toString()
  {
    return outputString + " (" + qName + ", " + length + ")";
  }
  
  
  
  
}