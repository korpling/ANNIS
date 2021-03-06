/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.htmlvis;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class OutputItem implements Comparable<OutputItem> // Comparator<OutputItem>,
{
  private String qName;
  private long length;
  private String outputString;
  private int priority;

  @Override
  public int compareTo(OutputItem o) {
    if (o == null) {
      throw new NullPointerException();
    }

    return ComparisonChain.start()
        // inverse order for length and priority
        // greater length --> smaller than the other item
        .compare(o.getLength(), length)
        // greater priority value --> smaller than the other item
        .compare(o.getPriority(), priority).compare(qName, o.getqName())
        .compare(outputString, o.getOutputString()).result();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof OutputItem) {
      return compareTo((OutputItem) obj) == 0;
    }
    return false;
  }

  public String getAnnoName() {
    return (qName.indexOf(":") > 0) ? qName.substring(qName.indexOf(":") + 2) : qName;
  }

  public long getLength() {
    return length;
  }

  public String getOutputString() {
    return outputString;
  }

  public int getPriority() {
    return priority;
  }

  public String getqName() {
    return qName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(length, priority, qName, outputString);
  }

  public void setLength(long length) {
    this.length = length;
  }

  public void setOutputString(String outputString) {
    this.outputString = outputString;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setqName(String qName) {
    this.qName = qName;
  }

  @Override
  public String toString() {
    return outputString + " (" + qName + ", " + length + ")";
  }


}
