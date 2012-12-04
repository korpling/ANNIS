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

import annis.gui.Helper;
import annis.gui.controlpanel.ExportPanel;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
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
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExportPanel.class);
  
  private Table tbResult;
  private String aql;
  private Set<String> corpora;
  private List<FrequencyTableEntry> freqDefinition;
  
  private ProgressIndicator pbQuery;

  public FrequencyResultPanel(String aql,
    Set<String> corpora,
    List<FrequencyTableEntry> freqDefinition)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.freqDefinition = freqDefinition;
    
    setSizeFull();
  }

  @Override
  public void attach()
  {
    super.attach();
    
    pbQuery = new ProgressIndicator();
    pbQuery.setCaption("Please wait, the frequencies analysis can take some time");
    pbQuery.setIndeterminate(true);
    pbQuery.setEnabled(true);
    
    addComponent(pbQuery);
  
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
          FrequencyTable table = get();
          recreateTable(table);
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
    
    if (getApplication() != null)
    {
      
      WebResource annisResource = Helper.getAnnisWebResource(getApplication());
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
        log.error(
          ex.getResponse().getEntity(String.class), ex);
      }
      catch (ClientHandlerException ex)
      {
        log.error("could not execute REST call to query matches", ex);
      }
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
    
    if(tbResult != null)
    {
      removeComponent(tbResult);
    }
    tbResult = new Table();
    tbResult.setSizeFull();
    
    if(!table.getEntries().isEmpty())
    {
      FrequencyTable.Entry firstEntry = table.getEntries().get(0);
      int tupelCount = firstEntry.getTupel().length;
      
      for(int i=1; i <= tupelCount; i++)
      {
        tbResult.addContainerProperty("tupel-" + i, String.class, "");
      }
      
      tbResult.addContainerProperty("count", Long.class, -1l);
      
      int line=0;
      for(FrequencyTable.Entry e : table.getEntries())
      {
        Object[] cells = new Object[tupelCount+1];
        System.arraycopy(e.getTupel(), 0, cells, 0, tupelCount);
        cells[cells.length-1] = e.getCount();
        tbResult.addItem(cells, "entry-" + line++);
      }
    };
    tbResult.addContainerProperty(pbQuery, null, table);
    
    addComponent(tbResult);
    
    pbQuery.setEnabled(true);
    removeComponent(pbQuery);
  }
  
  
}
