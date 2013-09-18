/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.widgets.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents one row in the grid view
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class Row implements Serializable
{
  private ArrayList<GridEvent> events;
  private BitSet occupancySet;
  private Set<String> textIDs;

  /**
   * Default constructor.
   */
  public Row()
  {
    this.events = new ArrayList<GridEvent>();
    this.textIDs = new HashSet<String>();

    occupancySet = new BitSet();
  }

  /**
   * Adds an event to this row
   * @param e
   * @return False if could not be added because the event is overlapping an
   *          other event in the row.
   */
  public boolean addEvent(GridEvent e)
  {
    BitSet eventOccupance = new BitSet(e.getRight());
    eventOccupance.set(e.getLeft(), e.getRight()+1, true);
    if(occupancySet.intersects(eventOccupance))
    {
      return false;
    }
    // set all bits to true that are covered by the other event
    occupancySet.or(eventOccupance);
    events.add(e);

    if(e.getTextID() != null && !e.getTextID().isEmpty())
    {
      textIDs.add(e.getTextID());
    }

    return true;
  }

  /**
   * Returns true if merge is possible.
   *
   * @see #merge(annis.gui.visualizers.component.grid.Row)
   */
  public boolean canMerge(Row other)
  {
    return !occupancySet.intersects(other.occupancySet);
  }

  /**
   * Merges the other row into this row.
   * This means all events from the other {@link Row} are added to this row. The other
   * {@link Row} will not be changed.
   * Only rows which have no overlapping events can be merged.
   *
   * @param other The other {@link Row} which will be merged into this one.
   * @return Returns true if merge was successfull, false if no merge was done.
   *
   * @see #canMerge(annis.gui.visualizers.component.grid.Row)
   */
  public boolean merge(Row other) throws IllegalArgumentException
  {
    if(canMerge(other))
    {
      occupancySet.or(other.occupancySet);
      for(GridEvent e : other.events)
      {
        events.add(e);
      }
      return true;
    }
    else
    {
      return false;
    }
  }

  public ArrayList<GridEvent> getEvents()
  {
    return events;
  }

  /**
   * Get Salt IDs of all texts used by events of this row.
   * @return
   */
  public Set<String> getTextIDs()
  {
    return new HashSet<String>(textIDs);
  }



}
