/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.objects;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;

/**
 * The query state of the actual displayed result query.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class DisplayedResultQuery extends PagedResultQuery {

  private static final String ASCENDING = "ascending";
  private static final String UNSORTED = "unsorted";
  private static final String RANDOM = "random";
  private static final String DESCENDING = "descending";
  /**
   * 
   */
  private static final long serialVersionUID = 6985425160111122181L;
  private Set<Long> selectedMatches = new TreeSet<>();
  private String baseText;

  public DisplayedResultQuery() {
    super();
  }

  public DisplayedResultQuery(DisplayedResultQuery orig) {
    super(orig);
    this.selectedMatches = orig.getSelectedMatches();
    this.baseText = orig.getBaseText();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DisplayedResultQuery other = (DisplayedResultQuery) obj;
    return Objects.equals(getQuery(), other.getQuery())
        && Objects.equals(getCorpora(), other.getCorpora())
        && Objects.equals(getLeftContext(), other.getLeftContext())
        && Objects.equals(getRightContext(), other.getRightContext())
        && Objects.equals(getSegmentation(), other.getSegmentation())
        && Objects.equals(getLimit(), other.getLimit())
        && Objects.equals(getOffset(), other.getOffset())
        && Objects.equals(getOrder(), other.getOrder())
        && Objects.equals(getBaseText(), other.getBaseText())
        && Objects.equals(getSelectedMatches(), other.getSelectedMatches());
  }

  public String getBaseText() {
    return baseText;
  }

  @Override
  public Map<String, String> getCitationFragmentArguments() {
    Map<String, String> result = super.getCitationFragmentArguments();

    // only output "bt" if it is not the same as the context segmentation
    if (!Objects.equals(getBaseText(), getSegmentation())) {
      result.put("_bt", (getBaseText() == null ? "" : getBaseText()));
    }
    if (getOrder() != OrderEnum.NORMAL && getOrder() != null) {
      if (getOrder() == OrderEnum.INVERTED) {
        result.put("o", DESCENDING);
      } else if (getOrder() == OrderEnum.RANDOMIZED) {
        result.put("o", RANDOM);
      } else if (getOrder() == OrderEnum.NOTSORTED) {
        result.put("o", UNSORTED);
      }

    }
    if (getSelectedMatches() != null && !getSelectedMatches().isEmpty()) {
      result.put("m", Joiner.on(',').join(getSelectedMatches()));
    }

    return result;
  }

  public static OrderEnum parseOrderFromCitationFragment(String value) {
    if (value != null) {
      switch (value.toLowerCase()) {
        case ASCENDING:
          return OrderEnum.NORMAL;
        case DESCENDING:
          return OrderEnum.INVERTED;
        case RANDOM:
          return OrderEnum.RANDOMIZED;
        case UNSORTED:
          return OrderEnum.NOTSORTED;
      }
    }
    return OrderEnum.NORMAL;
  }

  public Set<Long> getSelectedMatches() {
    return selectedMatches;
  }


  @Override
  public int hashCode() {
    return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(),
        getSegmentation(), getLimit(), getOffset(), getOrder(), getBaseText(),
        getSelectedMatches());
  }

  public void setBaseText(String baseText) {
    this.baseText = baseText;
  }

  public void setSelectedMatches(Set<Long> selectedMatches) {
    Preconditions.checkNotNull(selectedMatches,
        "The selected matches set of a paged result query must never be null (but can be empty)");
    this.selectedMatches = selectedMatches;
  }

  @Override
  public String toString() {
    return "search query [selectedMatches=" + selectedMatches + ", baseText=" + baseText
        + ", super=" + super.toString() + "]";
  }


}
