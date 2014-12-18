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

import annis.service.objects.OrderType;
import annis.service.objects.FrequencyTableQuery;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to construct new {@link Query} objects (or one of the child classes)
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @param <T> The type of the class to generate
 * @param <QG> The final type of the generator
 */
public class QueryGenerator<T extends Query, QG extends QueryGenerator<T, QG>>
{
  private final T current;
  
  /**
   * It's not allowed to construct a query generator on your own.
   */
  private QueryGenerator(T current)
  {
    this.current = current;
  }
  
  public static PagedQueryGenerator paged()
  {
    return new PagedQueryGenerator();
  }
  
  public static ExportQueryGenerator export()
  {
    return new ExportQueryGenerator();
  }
  
  public static FrequencyQueryGenerator frequency()
  {
    return new FrequencyQueryGenerator();
  }
  
  public QG query(String aql)
  {
    current.setQuery(aql);
    return (QG) this;
  }
  
  public QG corpora(Set<String> corpora)
  {
    current.setCorpora(new LinkedHashSet<>(corpora));
    return (QG) this;
  }
  
  
  protected T getCurrent()
  {
    return current;
  }
  
  public static class ContextQueryGenerator<T extends ContextualizedQuery, QG extends ContextQueryGenerator<T, QG>> 
    extends QueryGenerator<T, QG>
  { 
    private ContextQueryGenerator(T query)
    {
      super(query);
    }
    
    public QG left(int left)
    {
      getCurrent().setLeftContext(left);
      return (QG) this;
    }
    
    public QG right(int right)
    {
      getCurrent().setRightContext(right);
      return (QG) this;
    }
    public QG segmentation(String segmentation)
    {
      getCurrent().setSegmentation(segmentation);
      return (QG) this;
    }
    
  }
  
  public static class PagedQueryGenerator
     extends ContextQueryGenerator<PagedResultQuery, PagedQueryGenerator>
  {
    private PagedQueryGenerator()
    {
      super(new PagedResultQuery());
    }
    public PagedQueryGenerator limit(int limit)
    {
      getCurrent().setLimit(limit);
      return (PagedQueryGenerator) this;
    }
    
    public PagedQueryGenerator offset(int offset)
    {
      getCurrent().setOffset(offset);
      return (PagedQueryGenerator) this;
    }
    
    public PagedQueryGenerator order(OrderType order)
    {
      getCurrent().setOrder(order);
      return (PagedQueryGenerator) this;
    }
  }
  
  
  public static class ExportQueryGenerator
     extends ContextQueryGenerator<ExportQuery, ExportQueryGenerator>
  {
    private ExportQueryGenerator()
    {
      super(new ExportQuery());
    }
    public ExportQueryGenerator exporter(String name)
    {
      getCurrent().setExporterName(name);
      return this;
    }
    
    public ExportQueryGenerator annotations(List<String> annotationKeys)
    {
      getCurrent().setAnnotationKeys(annotationKeys);
      return this;
    }
    
    public ExportQueryGenerator param(String parameters)
    {
      getCurrent().setParameters(parameters);
      return this;
    }
  }
  
  public static class FrequencyQueryGenerator
     extends QueryGenerator<FrequencyQuery, FrequencyQueryGenerator>
  {
    private FrequencyQueryGenerator()
    {
      super(new FrequencyQuery());
    }
    
     public FrequencyQueryGenerator def(FrequencyTableQuery def)
    {
      getCurrent().setFrequencyDefinition(def);
      return this;
    }
    
  }
  
  
  public T build()
  {
    return current;
  }
  
}
