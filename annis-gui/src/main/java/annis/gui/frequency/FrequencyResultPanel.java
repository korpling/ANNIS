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
package annis.gui.frequency;

import annis.gui.components.FrequencyChart;
import annis.libgui.Helper;
import annis.libgui.PollControl;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Container;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyResultPanel extends VerticalLayout
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(FrequencyResultPanel.class);
  
  public static final int MAX_NUMBER_OF_CHART_ITEMS = 500;
  
  private Table tblResult;
  private Button btDownloadCSV;
  FrequencyChart chart;
  private String aql;
  private Set<String> corpora;
  private List<FrequencyTableEntry> freqDefinition;
  final FrequencyQueryPanel queryPanel;
  
  private ProgressBar pbQuery;

  public FrequencyResultPanel(String aql,
    Set<String> corpora,
    final List<FrequencyTableEntry> freqDefinition, FrequencyQueryPanel queryPanel)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.freqDefinition = freqDefinition;
    this.queryPanel = queryPanel;
    
    setSizeFull();
    
    pbQuery = new ProgressBar();
    pbQuery.setCaption("Please wait, the frequencies analysis can take some time");
    pbQuery.setIndeterminate(true);
    pbQuery.setEnabled(true);
    
    addComponent(pbQuery);
    setComponentAlignment(pbQuery, Alignment.TOP_CENTER);
  
    chart = new FrequencyChart(this);
    chart.setHeight("350px");
    chart.setVisible(false);
    addComponent(chart);
    
    
    btDownloadCSV = new Button("Download as CSV");
    btDownloadCSV.setDescription("Download as CSV");
    btDownloadCSV.setSizeUndefined();
    addComponent(btDownloadCSV);
    setComponentAlignment(btDownloadCSV, Alignment.TOP_RIGHT);
    
    btDownloadCSV.setVisible(false);
    btDownloadCSV.setIcon(new ThemeResource("../runo/icons/16/document-txt.png"));
    btDownloadCSV.addStyleName(ChameleonTheme.BUTTON_SMALL);
    
    final UI ui = UI.getCurrent();
    // actually start query
    Callable<FrequencyTable> r = new Callable<FrequencyTable>() 
    {
      @Override
      public FrequencyTable call() throws Exception
      {
        final FrequencyTable t = loadBeans();
        
        ui.access(new Runnable()
        {

          @Override
          public void run()
          {
            showResult(t);
          }
        });
        
        return t;
      } 
    };
    
    PollControl.callInBackground(1000, null, r);
  }
  
  private FrequencyTable loadBeans()
  {
    FrequencyTable result = new FrequencyTable();
      
    WebResource annisResource = Helper.getAnnisWebResource();
    try
    {
      annisResource = annisResource.path("query").path("search").path("frequency")
        .queryParam("q", aql)  
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .queryParam("fields", createFieldsString());

      result = annisResource.get(FrequencyTable.class);
    }
    catch (UniformInterfaceException ex)
    {
      String message;
      if (ex.getResponse().getStatus() == 400)
      {
        message = ex.getResponse().getEntity(String.class);
      }
      else if (ex.getResponse().getStatus() == 504) // gateway timeout
      {
        message = "Timeout: query exeuction took too long";
      }
      else
      {
        message = "unknown error: " + ex;
        log.error(ex.getResponse().getEntity(String.class), ex);
      }
      Notification.show(message, Notification.Type.WARNING_MESSAGE);      
    }
    catch (ClientHandlerException ex)
    {
      log.error("could not execute REST call to query frequency", ex);
    }

    return result;
  }
  
  private void showResult(FrequencyTable table)
  {
    if (queryPanel != null)
    {
      queryPanel.notifiyQueryFinished();
    }
    recreateTable(table);

    btDownloadCSV.setVisible(true);
    FileDownloader downloader = new FileDownloader(
      new StreamResource(new CSVResource(table, freqDefinition),
        "frequency.txt"));
    downloader.extend(btDownloadCSV);

    chart.setVisible(true);
    FrequencyTable clippedTable = table;
    if (clippedTable.getEntries().size() > MAX_NUMBER_OF_CHART_ITEMS)
    {
      List<FrequencyTable.Entry> entries
        = new ArrayList<FrequencyTable.Entry>(clippedTable.getEntries());

      clippedTable = new FrequencyTable();
      clippedTable.setEntries(entries.subList(0,
        MAX_NUMBER_OF_CHART_ITEMS));
      clippedTable.setSum(table.getSum());
      chart.setCaption(
        "Showing historgram of top 500 results, see table below for complete dataset.");
    }
    chart.setFrequencyData(clippedTable);


  }
  
  private String createFieldsString()
  {    
    StringBuilder sb = new StringBuilder();
    
    ListIterator<FrequencyTableEntry> it = freqDefinition.listIterator();
    while(it.hasNext())
    {
      FrequencyTableEntry e = it.next();
      
      sb.append(e.getReferencedNode()).append(":");
      if(e.getType() == FrequencyTableEntryType.span)
      {
        sb.append("tok");
      }
      else
      {
        sb.append(e.getKey());
      }
      
      if(it.hasNext())
      {
        sb.append(",");
      }
    }
    
    return sb.toString();
  }
  
  private void recreateTable(FrequencyTable table)
  {
    
    if(tblResult != null)
    {
      removeComponent(tblResult);
    }
    
    tblResult = new Table();
    tblResult.setSizeFull();
    
    tblResult.setCaption(table.getEntries().size() 
      + " items with a total sum of " + table.getSum()
      + " (query on " + Joiner.on(", ").join(this.corpora) + ")"
    );
    
    tblResult.setSelectable(true);
    tblResult.setMultiSelect(false);
    tblResult.addStyleName("corpus-font-force");
    
    if(!table.getEntries().isEmpty())
    {
      FrequencyTable.Entry firstEntry = table.getEntries().iterator().next();
      int tupelCount = firstEntry.getTupel().length;
      
      tblResult.addContainerProperty("rank", Integer.class, -1);
      for(int i=1; i <= tupelCount; i++)
      {
        tblResult.addContainerProperty("tupel-" + i, String.class, "");
        FrequencyTableEntry e = freqDefinition.get(i-1);
        String caption = "#" + e.getReferencedNode() + " ("
          + (e.getType() == FrequencyTableEntryType.span 
            ? "spanned text" : e.getKey())
          + ")";
        
        tblResult.setColumnHeader("tupel-"+ i, caption);
      }
      
      tblResult.addContainerProperty("count", Long.class, -1l);
      
      int line=0;
      for(FrequencyTable.Entry e : table.getEntries())
      {
        Object[] cells = new Object[tupelCount+2];
        
        System.arraycopy(e.getTupel(), 0, cells, 1, tupelCount);
        
        cells[0] = line+1;
        cells[cells.length-1] = e.getCount();
        
        tblResult.addItem(cells, "entry-" + line++);
      }
    };
    tblResult.addContainerProperty(pbQuery, null, table);
    addLexicalSort(tblResult.getContainerDataSource());
    
    addComponent(tblResult);
    setExpandRatio(tblResult, 1.0f);
    
    pbQuery.setEnabled(true);
    removeComponent(pbQuery);
  }
  
  private void addLexicalSort(Container container)
  {
    ItemSorter sorter = new DefaultItemSorter(new IgnoreCaseComparator());
    
    if(container instanceof IndexedContainer)
    {
      ((IndexedContainer) container).setItemSorter(sorter);
    }
    else if(container instanceof AbstractBeanContainer)
    {
      ((AbstractBeanContainer) container).setItemSorter(sorter);
    }
  }
  
  public static class IgnoreCaseComparator implements
    Comparator<Object>, Serializable
  {

    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2)
    {
      if(o1 instanceof String && o2 instanceof String)
      {
        String s1 = (String) o1;
        String s2 = (String) o2;
        
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(s1, s2);
      }
      else
      {
        return Ordering.natural().compare((Comparable) o1, (Comparable) o2);
      }
    }
  }
  
  public static class CSVResource implements StreamResource.StreamSource
  {
    private final FrequencyTable data;
    private final List<FrequencyTableEntry> freqDefinition;
    public CSVResource(FrequencyTable data, List<FrequencyTableEntry> freqDefinition)
    {
      this.data = data;
      this.freqDefinition = freqDefinition;
    }

    @Override
    public InputStream getStream()
    {
      try
      {
        File tmpFile = File.createTempFile("annis-frequency", ".txt");
        tmpFile.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), Charsets.UTF_8);
        
        CSVWriter csv = new CSVWriter(writer, '\t', CSVWriter.NO_QUOTE_CHARACTER, '\\');
        
        // write headers
        ArrayList<String> header = new ArrayList<String>();
        if(data.getEntries().size() > 0)
        {
          for(int i=0; i < data.getEntries().iterator().next().getTupel().length; i++)
          {
            FrequencyTableEntry e = freqDefinition.get(i);
            String caption = "#" + e.getReferencedNode() + " ("
              + (e.getType() == FrequencyTableEntryType.span
              ? "spanned text" : e.getKey())
              + ")";
            
            header.add(caption);
          }
        }
        // add count
        header.add("count");
        csv.writeNext(header.toArray(new String[0]));
        
        // write entries
        for (FrequencyTable.Entry e : data.getEntries())
        {
          ArrayList<String> d = new ArrayList<String>();
          d.addAll(Arrays.asList(e.getTupel()));
          d.add("" + e.getCount());
          csv.writeNext(d.toArray(new String[0]));
        }
        writer.close();
        
        return new FileInputStream(tmpFile);

      }
      catch (IOException ex)
      {
        log.error(null, ex);
      }
      return new ByteArrayInputStream(new byte[0]);
    }
  }
  
  
  public void selectRow(int i)
  {
    tblResult.setValue("entry-" + i);
    tblResult.setCurrentPageFirstItemId("entry-" + i);
  }
  
  
}
