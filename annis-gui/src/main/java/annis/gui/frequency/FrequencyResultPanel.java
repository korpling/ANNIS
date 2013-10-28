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
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
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
  
  private ProgressIndicator pbQuery;

  public FrequencyResultPanel(String aql,
    Set<String> corpora,
    final List<FrequencyTableEntry> freqDefinition, final FrequencyQueryPanel queryPanel)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.freqDefinition = freqDefinition;
    
    setSizeFull();
    
    pbQuery = new ProgressIndicator();
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
    
    // actually start query
    Callable<FrequencyTable> r = new Callable<FrequencyTable>() 
    {
      @Override
      public FrequencyTable call() throws Exception
      {
        return loadBeans();
      } 
    };
    
    FutureTask<FrequencyTable> task = new FutureTask<FrequencyTable>(r)
    {
      @Override
      protected void done()
      {
        super.done();
        try
        {
          if(queryPanel != null)
          {
            queryPanel.notifiyQueryFinished();
          }
          FrequencyTable table = get();
          recreateTable(table);
          
          btDownloadCSV.setVisible(true);
          FileDownloader downloader = new FileDownloader(
            new StreamResource(new CSVResource(table, freqDefinition), 
            "frequency.csv"));
          downloader.extend(btDownloadCSV);
          
          chart.setVisible(true);
          FrequencyTable clippedTable = table;
          if(clippedTable.getEntries().size() > MAX_NUMBER_OF_CHART_ITEMS)
          {
            List<FrequencyTable.Entry> entries = 
              new ArrayList<FrequencyTable.Entry>(clippedTable.getEntries());
            
            clippedTable = new FrequencyTable();
            clippedTable.setEntries(entries.subList(0,
              MAX_NUMBER_OF_CHART_ITEMS));
            clippedTable.setSum(table.getSum());
            chart.setCaption("Showing historgram of top 500 results, see table below for complete dataset.");
          }
          chart.setFrequencyData(clippedTable);
          
        }
        catch (InterruptedException ex)
        {
          log.error(null, ex);
        }
        catch (ExecutionException ex)
        {
          log.error(null, ex);
        }
        
      }
    };
    
    Executor exec = Executors.newSingleThreadExecutor();
    exec.execute(task);
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
        message = "parsing error: "
          + ex.getResponse().getEntity(String.class);
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
    
    addComponent(tblResult);
    setExpandRatio(tblResult, 1.0f);
    
    pbQuery.setEnabled(true);
    removeComponent(pbQuery);
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
        File tmpFile = File.createTempFile("annis-frequency", ".csv");
        tmpFile.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), Charsets.UTF_8);
        
        CSVWriter csv = new CSVWriter(writer);
        
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
