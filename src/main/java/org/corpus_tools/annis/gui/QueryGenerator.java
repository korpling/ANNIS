/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.gui.objects.ContextualizedQuery;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.objects.FrequencyQuery;
import org.corpus_tools.annis.gui.objects.FrequencyTableQuery;
import org.corpus_tools.annis.gui.objects.PagedResultQuery;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryLanguage;

/**
 * Helper class to construct new {@link Query} objects (or one of the child classes)
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @param <T> The type of the class to generate
 * @param <QG> The final type of the generator
 */
public class QueryGenerator<T extends Query, QG extends QueryGenerator<T, QG>> {
  public static class ContextQueryGenerator<T extends ContextualizedQuery, QG extends ContextQueryGenerator<T, QG>>
      extends QueryGenerator<T, QG> {
    protected ContextQueryGenerator(T query) {
      super(query);
    }

    public QG left(int left) {
      getCurrent().setLeftContext(left);
      return (QG) this;
    }

    public QG right(int right) {
      getCurrent().setRightContext(right);
      return (QG) this;
    }

    public QG segmentation(String segmentation) {
      getCurrent().setSegmentation(segmentation);
      return (QG) this;
    }

  }

  public static class DisplayedResultQueryGenerator
      extends PagedQueryGenerator<DisplayedResultQuery, DisplayedResultQueryGenerator> {
    private DisplayedResultQueryGenerator() {
      super(new DisplayedResultQuery());
    }

    public DisplayedResultQueryGenerator baseText(String val) {
      getCurrent().setBaseText(val);
      return this;
    }

    public DisplayedResultQueryGenerator selectedMatches(Set<Long> selected) {
      getCurrent().setSelectedMatches(selected == null ? new TreeSet<Long>() : selected);
      return this;
    }
  }

  public static class FrequencyQueryGenerator
      extends QueryGenerator<FrequencyQuery, FrequencyQueryGenerator> {
    FrequencyQueryGenerator() {
      super(new FrequencyQuery());
    }

    public FrequencyQueryGenerator def(FrequencyTableQuery def) {
      getCurrent().setFrequencyDefinition(def);
      return this;
    }

  }


  public static class PagedQueryGenerator<T extends PagedResultQuery, QG extends PagedQueryGenerator<T, QG>>
      extends ContextQueryGenerator<T, QG> {
    private PagedQueryGenerator(T query) {
      super(query);
    }

    public QG limit(int limit) {
      getCurrent().setLimit(limit);
      return (QG) this;
    }

    public QG offset(long offset) {
      getCurrent().setOffset(offset);
      return (QG) this;
    }

    public QG order(OrderEnum order) {
      getCurrent().setOrder(order);
      return (QG) this;
    }

  }

  public static DisplayedResultQueryGenerator displayed() {
    return new DisplayedResultQueryGenerator();
  }

  public static FrequencyQueryGenerator frequency() {
    return new FrequencyQueryGenerator();
  }

  private final T current;

  /**
   * It's not allowed to construct a query generator on your own.
   */
  private QueryGenerator(T current) {
    this.current = current;
  }

  public T build() {
    return current;
  }

  public QG corpora(Set<String> corpora) {
    current.setCorpora(new LinkedHashSet<>(corpora));
    return (QG) this;
  }

  protected T getCurrent() {
    return current;
  }

  public QG query(String aql) {
    current.setQuery(aql);
    return (QG) this;
  }

  public QG queryLanguage(QueryLanguage queryLanguage) {
    current.setQueryLanguage(queryLanguage);;
    return (QG) this;
  }

}
