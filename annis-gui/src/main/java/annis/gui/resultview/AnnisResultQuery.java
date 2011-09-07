/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
import annis.service.objects.AnnisResultImpl;
import com.vaadin.ui.Window;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

/**
 *
 * @author thomas
 */
public class AnnisResultQuery extends AbstractBeanQuery<AnnisResult> implements Serializable
{

  public AnnisResultQuery(QueryDefinition definition, Map<String, Object> queryConfiguration, Object[] sortPropertyIds, boolean[] sortStates)
  {
    super(definition, queryConfiguration, sortPropertyIds, sortStates);
  }

  @Override
  protected AnnisResult constructBean()
  {
    return new AnnisResultImpl();
  }

  @Override
  public int size()
  {
    Integer count = (Integer) getQueryConfiguration().get("count");
    Integer pageSize = (Integer) getQueryConfiguration().get("pageSize");
    if(count == null || pageSize == null)
    {
      return 0;
    }
    if(count < 0)
    {
      return pageSize;
    }
    else
    {
      return count;
    }
  }

  @Override
  protected List<AnnisResult> loadBeans(int startIndex, int count)
  {
    AnnisService service = (AnnisService) getQueryConfiguration().get("service");
    Window window = (Window) getQueryConfiguration().get("window");
    List<Long> corpora = (List<Long>) getQueryConfiguration().get("corpora");
    String aql = (String) getQueryConfiguration().get("aql");
    int contextLeft = (Integer) getQueryConfiguration().get("contextLeft");
    int contextRight = (Integer) getQueryConfiguration().get("contextRight");
    
    List<AnnisResult> result = new LinkedList<AnnisResult>();
    if(service != null)
    {
      try
      {
        result.addAll(service.getResultSet(corpora, aql, count, startIndex, contextLeft, contextRight));
      }
      catch(RemoteException ex)
      {
        Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE, null, ex);
      }      
      catch(AnnisQLSemanticsException ex)
      {
         Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch(AnnisQLSyntaxException ex)
      {
        window.showNotification("Syntax error: " + ex.getLocalizedMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
      }
      catch(AnnisCorpusAccessException ex)
      {
        window.showNotification("Corpus access error: " + ex.getLocalizedMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
      }
    }
    return result;
  }

  @Override
  protected void saveBeans(List<AnnisResult> addedBeans, List<AnnisResult> modifiedBeans, List<AnnisResult> removedBeans)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
