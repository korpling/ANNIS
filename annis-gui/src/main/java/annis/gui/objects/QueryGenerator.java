/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.objects;

import com.google.common.base.Splitter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to construct new {@link Query} objects (or one of the child classes)
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @param <T>
 */
public class QueryGenerator<T extends Query>
{
  private T current;
  
  /**
   * It's not allowed to construct a query generator on your own.
   */
  private QueryGenerator(T current)
  {
    this.current = current;
  }
  
  public static QueryGenerator<PagedResultQuery> paged()
  {
    QueryGenerator<PagedResultQuery> gen = new PagedQueryGenerator<>(new PagedResultQuery());
    gen.current = new PagedResultQuery();
    return gen;
  }
  
  public static ExportQueryGenerator<? extends ExportQuery> export()
  {
    ExportQueryGenerator<ExportQuery> gen = new ExportQueryGenerator<>(new ExportQuery());
    return gen;
  }
  
  public static FrequencyQueryGenerator<? extends FrequencyQuery> frequency()
  {
    FrequencyQueryGenerator<FrequencyQuery> gen = new FrequencyQueryGenerator<>(new FrequencyQuery());
    return gen;
  }
  
  public QueryGenerator<? extends T> query(String aql)
  {
    current.setQuery(aql);
    return this;
  }
  
  public QueryGenerator<? extends T> corpora(Set<String> corpora)
  {
    current.setCorpora(new LinkedHashSet<>(corpora));
    return this;
  }
  
  protected T getCurrent()
  {
    return current;
  }
  
  public static class ContextQueryGenerator<T extends ContextualizedQuery> extends QueryGenerator<T>
  { 
    private ContextQueryGenerator(T query)
    {
      super(query);
    }
    public ContextQueryGenerator<? extends ContextualizedQuery> left(int left)
    {
      getCurrent().setContextLeft(left);
      return this;
    }
    public ContextQueryGenerator<? extends ContextualizedQuery> right(int right)
    {
      getCurrent().setContextRight(right);
      return this;
    }
    public ContextQueryGenerator<? extends ContextualizedQuery> segmentation(String segmentation)
    {
      getCurrent().setSegmentation(segmentation);
      return this;
    }
    
  }
  
  public static class PagedQueryGenerator<T extends PagedResultQuery> extends ContextQueryGenerator<T>
  {
    private PagedQueryGenerator(T query)
    {
      super(query);
    }
    public PagedQueryGenerator<? extends PagedResultQuery> limit(int limit)
    {
      getCurrent().setLimit(limit);
      return this;
    }
    
    public PagedQueryGenerator<? extends PagedResultQuery> offset(int offset)
    {
      getCurrent().setOffset(offset);
      return this;
    }
  }
  
  public static class ExportQueryGenerator<T extends ExportQuery> extends ContextQueryGenerator<T>
  {
    private ExportQueryGenerator(T query)
    {
      super(query);
    }
    public ExportQueryGenerator<? extends ExportQuery> exporter(String name)
    {
      getCurrent().setExporterName(name);
      return this;
    }
    
    public ExportQueryGenerator<? extends ExportQuery> annotations(String annotationKeys)
    {
      List<String> asList = Splitter.on(',').omitEmptyStrings()
        .trimResults().splitToList(annotationKeys);
      getCurrent().setAnnotationKeys(asList);
      return this;
    }
    
    public ExportQueryGenerator<? extends ExportQuery> param(String parameters)
    {
      getCurrent().setParameters(parameters);
      return this;
    }
  }
  
  public static class FrequencyQueryGenerator<T extends FrequencyQuery> extends QueryGenerator<T>
  {
    private FrequencyQueryGenerator(T query)
    {
      super(query);
    }
    
  }
  
  
  public T build()
  {
    return current;
  }
  
}
