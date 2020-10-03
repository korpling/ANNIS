/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.widgets.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An event has a right and left border (but might have holes)
 *
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GridEvent implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 7258734016705087828L;

  private String id;

  private int left;

  private int right;

  private String value;

  private Long match;

  private final List<String> coveredIDs;

  // used for a tooltip in the frontend
  private String tooltip;

  private Double startTime;

  private Double endTime;

  private boolean gap;
  private boolean space;

  private String textID;

  private String pageNumber;

  /**
   * Copy constructor
   * 
   * @param orig
   */
  public GridEvent(GridEvent orig) {
    this.id = orig.id;
    this.value = orig.value;
    this.left = orig.left;
    this.right = orig.right;
    this.match = orig.match;
    this.coveredIDs = new ArrayList<>(orig.coveredIDs);
    this.tooltip = orig.tooltip;
    this.startTime = orig.startTime;
    this.endTime = orig.endTime;
    this.gap = orig.gap;
    this.space = orig.space;
    this.textID = orig.textID;
    this.pageNumber = orig.pageNumber;
  }

  /**
   * Inits a new GridEvent.
   *
   * @param sID This is the salt id of the node, sppan or tok. Used for identifying highlighted
   *        nodes with {@link #getCoveredIDs()}
   * @param left the most left token index
   * @param right the most right token index
   * @param value the value displayed in the table row
   */
  public GridEvent(String sID, int left, int right, String value) {
    this.id = sID;
    this.left = left;
    this.right = right;
    this.value = value;

    this.coveredIDs = new LinkedList<>();
  }

  public List<String> getCoveredIDs() {
    return coveredIDs;
  }

  public Double getEndTime() {
    return endTime;
  }

  public String getId() {
    return id;
  }

  public int getLeft() {
    return left;
  }

  public Long getMatch() {
    return match;
  }

  public String getPageNumber() {
    return pageNumber;
  }

  public int getRight() {
    return right;
  }

  public Double getStartTime() {
    return startTime;
  }

  /**
   * Salt ID of the text this event belongs to.
   *
   * @return
   */
  public String getTextID() {
    return textID;
  }

  /**
   * Returns the tooltip, which should be displayed when hovering this event.
   *
   * @return The tooltip value could be null.
   */
  public String getTooltip() {
    return tooltip;
  }

  public String getValue() {
    return value;
  }

  public boolean isGap() {
    return gap;
  }

  public boolean isSpace() {
    return space;
  }

  public void setEndTime(Double endTime) {
    this.endTime = endTime;
  }

  public void setGap(boolean gap) {
    this.gap = gap;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLeft(int left) {
    this.left = left;
  }

  public void setMatch(Long match) {
    this.match = match;
  }

  public void setPage(String pageNumber) {
    this.pageNumber = pageNumber;
  }

  public void setRight(int right) {
    this.right = right;
  }

  public void setSpace(boolean space) {
    this.space = space;
  }

  public void setStartTime(Double startTime) {
    this.startTime = startTime;
  }

  public void setTextID(String textID) {
    this.textID = textID;
  }

  /**
   * Sets the tooltip which is display when hovering this event.
   *
   * @param tooltip the title to set, could be null.
   */
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "" + id + " -> " + value + " (" + left + "-" + right + ")";
  }


}
