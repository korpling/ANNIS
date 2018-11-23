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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;

import annis.gui.controlpanel.CorpusSorter;
import annis.gui.exporter.CSVExporter;
import annis.gui.frequency.UserGeneratedFrequencyEntry;
import annis.libgui.exporter.ExporterPlugin;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.OrderType;

/**
 * Helper class to bundle all query relevant state information of the UI.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryUIState implements Serializable
{
  
  public enum QueryType {COUNT, FIND, FREQUENCY, EXPORT}
  
  
  private final ObjectProperty<String> aql = new ObjectProperty<>("");
  private final ObjectProperty<Set<String>> selectedCorpora 
    = new ObjectProperty<Set<String>>(new LinkedHashSet<String>());
  
  private final ObjectProperty<Integer> leftContext = new ObjectProperty<>(5);
  private final ObjectProperty<Integer> rightContext = new ObjectProperty<>(5);
  
  private final ObjectProperty<Integer> limit = new ObjectProperty<>(10);
  private final ObjectProperty<Long> offset = new ObjectProperty<>(0l);
  private final ObjectProperty<String> visibleBaseText = new ObjectProperty<>(null, String.class);
  private final ObjectProperty<String> contextSegmentation = new ObjectProperty<>(null, String.class);
  
  private final ObjectProperty<OrderType> order = new ObjectProperty<>(OrderType.ascending);
  
  private final ObjectProperty<Set<Long>> selectedMatches = 
    new ObjectProperty<Set<Long>>(new TreeSet<Long>());
  
  private final ObjectProperty<Class<? extends ExporterPlugin>> exporter = new ObjectProperty<Class<? extends ExporterPlugin>>(CSVExporter.class);
  private final ObjectProperty<List<String>> exportAnnotationKeys 
    = new ObjectProperty<List<String>>(new ArrayList<String>());
  private final ObjectProperty<String> exportParameters = 
    new ObjectProperty<>("");
  
  private final ObjectProperty<Boolean> alignmc = new ObjectProperty<Boolean>(false);
  
  private transient Map<QueryType, Future<?>> executedTasks;
  
  private final BeanContainer<Integer, UserGeneratedFrequencyEntry> frequencyTableDefinition
    = new BeanContainer<>(UserGeneratedFrequencyEntry.class);
  private final ObjectProperty<Set<String>> frequencyMetaData = 
    new ObjectProperty<Set<String>> (new TreeSet<String>());
  
  private final BeanItemContainer<Query> history = new BeanItemContainer<>(Query.class);
  
  private final BeanContainer<String, AnnisCorpus> availableCorpora = new BeanContainer<>(AnnisCorpus.class);;
  
  public QueryUIState()
  {
    initTransients();
    availableCorpora.setBeanIdProperty("name");
    availableCorpora.setItemSorter(new CorpusSorter());
  }
  
  private void initTransients()
  {
    executedTasks = new EnumMap<>(QueryType.class);
  }
  
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initTransients();
  }
  
  public ObjectProperty<String> getAql()
  {
    return aql;
  }

  public ObjectProperty<Set<String>> getSelectedCorpora()
  {
    return selectedCorpora;
  }

  public ObjectProperty<Integer> getLeftContext()
  {
    return leftContext;
  }

  public ObjectProperty<Integer> getRightContext()
  {
    return rightContext;
  }

  public ObjectProperty<Integer> getLimit()
  {
    return limit;
  }

  public ObjectProperty<Long> getOffset()
  {
    return offset;
  }

  public ObjectProperty<Set<Long>> getSelectedMatches()
  {
    return selectedMatches;
  }
  
  public ObjectProperty<String> getVisibleBaseText()
  {
    return visibleBaseText;
  }

  public ObjectProperty<String> getContextSegmentation()
  {
    return contextSegmentation;
  }
  

  public Map<QueryType, Future<?>> getExecutedTasks()
  {
    return executedTasks;
  }

  public BeanItemContainer<Query> getHistory()
  {
    return history;
  }  

  public ObjectProperty<Class<? extends ExporterPlugin>> getExporter()
  {
    return exporter;
  }

  public ObjectProperty<List<String>> getExportAnnotationKeys()
  {
    return exportAnnotationKeys;
  }

  public ObjectProperty<String> getExportParameters()
  {
    return exportParameters;
  }
  
  public ObjectProperty<Boolean> getAlignmc()
  {
    return alignmc;
  }

  public BeanContainer<Integer, UserGeneratedFrequencyEntry> getFrequencyTableDefinition()
  {
    return frequencyTableDefinition;
  }

  public ObjectProperty<Set<String>> getFrequencyMetaData()
  {
    return frequencyMetaData;
  }

  public ObjectProperty<OrderType> getOrder()
  {
    return order;
  }

  public BeanContainer<String, AnnisCorpus> getAvailableCorpora()
  {
    return availableCorpora;
  }
  
  
 
}
