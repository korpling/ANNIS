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
package org.corpus_tools.annis.gui.objects;

import com.vaadin.v7.data.util.BeanContainer;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.ObjectProperty;
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
import org.apache.http.concurrent.Cancellable;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.gui.exporter.CSVExporter;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;
import org.corpus_tools.annis.gui.frequency.UserGeneratedFrequencyEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to bundle all query relevant state information of the UI.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class QueryUIState implements Serializable {

  public enum QueryType {
    COUNT, FIND, FREQUENCY, EXPORT
  }

  /**
   * 
   */
  private static final long serialVersionUID = -883486391491336806L;

  private final static Logger log = LoggerFactory.getLogger(QueryUIState.class);

  private final ObjectProperty<String> aql = new ObjectProperty<>("");
  private Set<String> selectedCorpora = new LinkedHashSet<String>();

  private int leftContext = 5;
  private int rightContext = 5;

  private int limit = 10;
  private final ObjectProperty<Long> offset = new ObjectProperty<>(0l);
  private final ObjectProperty<String> visibleBaseText = new ObjectProperty<>(null, String.class);
  private String contextSegmentation = null;

  private OrderEnum order = OrderEnum.NORMAL;

  private QueryLanguage queryLanguage = QueryLanguage.AQL;

  private final ObjectProperty<Set<Long>> selectedMatches =
      new ObjectProperty<Set<Long>>(new TreeSet<Long>());

  private Class<? extends ExporterPlugin> exporter = CSVExporter.class;
  private List<String> exportAnnotationKeys = new ArrayList<>();
  private String exportParameters = "";

  private final ObjectProperty<Boolean> alignmc = new ObjectProperty<Boolean>(false);

  private transient Map<QueryType, Future<?>> executedTasks;

  private transient Map<QueryType, Cancellable> executedCalls;

  private final BeanContainer<Integer, UserGeneratedFrequencyEntry> frequencyTableDefinition =
      new BeanContainer<>(UserGeneratedFrequencyEntry.class);

  private final BeanItemContainer<Query> history = new BeanItemContainer<>(Query.class);

  public QueryUIState() {
    initTransients();
  }

  public ObjectProperty<Boolean> getAlignmc() {
    return alignmc;
  }

  public ObjectProperty<String> getAql() {
    return aql;
  }

  public String getContextSegmentation() {
    return contextSegmentation;
  }

  public void setContextSegmentation(String contextSegmentation) {
    this.contextSegmentation = contextSegmentation;
  }

  public Map<QueryType, Future<?>> getExecutedTasks() {
    return executedTasks;
  }

  public Map<QueryType, Cancellable> getExecutedCalls() {
    return executedCalls;
  }

  public List<String> getExportAnnotationKeys() {
    return exportAnnotationKeys;
  }

  public void setExportAnnotationKeys(List<String> keys) {
    this.exportAnnotationKeys = keys;
  }


  public void setExportParameters(String exportParameters) {
    this.exportParameters = exportParameters;
  }

  public Class<? extends ExporterPlugin> getExporter() {
    return exporter;
  }

  public void setExporter(Class<? extends ExporterPlugin> exporter) {
    this.exporter = exporter;
  }

  public String getExportParameters() {
    return exportParameters;
  }

  public BeanContainer<Integer, UserGeneratedFrequencyEntry> getFrequencyTableDefinition() {
    return frequencyTableDefinition;
  }

  public BeanItemContainer<Query> getHistory() {
    return history;
  }

  public int getLeftContext() {
    return leftContext;
  }

  public void setLeftContext(int leftContext) {
    this.leftContext = leftContext;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public ObjectProperty<Long> getOffset() {
    return offset;
  }

  public OrderEnum getOrder() {
    return order;
  }

  public void setOrder(OrderEnum order) {
    this.order = order;
  }

  public QueryLanguage getQueryLanguage() {
    return queryLanguage;
  }

  public org.corpus_tools.annis.gui.objects.QueryLanguage getQueryLanguageLegacy() {
    if (queryLanguage == QueryLanguage.AQLQUIRKSV3) {
      return org.corpus_tools.annis.gui.objects.QueryLanguage.AQL_QUIRKS_V3;
    } else {
      return org.corpus_tools.annis.gui.objects.QueryLanguage.AQL;
    }
  }

  public void setQueryLanguage(QueryLanguage queryLanguage) {
    this.queryLanguage = queryLanguage;
  }

  public void setQueryLanguageLegacy(org.corpus_tools.annis.gui.objects.QueryLanguage queryLanguage) {
    if (queryLanguage == org.corpus_tools.annis.gui.objects.QueryLanguage.AQL_QUIRKS_V3) {
      this.queryLanguage = QueryLanguage.AQLQUIRKSV3;
    } else {
      this.queryLanguage = QueryLanguage.AQL;
    }
  }

  public int getRightContext() {
    return rightContext;
  }

  public void setRightContext(int rightContext) {
    this.rightContext = rightContext;
  }

  public Set<String> getSelectedCorpora() {
    return selectedCorpora;
  }

  public void setSelectedCorpora(Set<String> selectedCorpora) {
    this.selectedCorpora = selectedCorpora;
  }

  public ObjectProperty<Set<Long>> getSelectedMatches() {
    return selectedMatches;
  }

  public ObjectProperty<String> getVisibleBaseText() {
    return visibleBaseText;
  }

  private void initTransients() {
    executedTasks = new EnumMap<>(QueryType.class);
    executedCalls = new EnumMap<>(QueryType.class);
  }

  private void readObject(final java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    initTransients();
  }

}
