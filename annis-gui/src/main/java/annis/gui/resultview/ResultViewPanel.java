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
import annis.gui.ServiceHelper;
import annis.service.AnnisService;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Window;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel
{
  private ProgressIndicator piResult;
  private Label lblInfo;
  
  private String aql;
  private Set<Long> corpora;
  private int contextLeft, contextRight;
  
  public ResultViewPanel(String aql, Set<Long> corpora, int contextLeft, int contextRight)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    
    piResult = new ProgressIndicator();
    piResult.setIndeterminate(true);
    piResult.setVisible(true);
    piResult.setEnabled(true);
    
    lblInfo = new Label();
    lblInfo.setValue("Result for query \"" + aql.replaceAll("\n", " ") + " is calculated.");
    
    addComponent(piResult);
    addComponent(lblInfo);
    
  }

  @Override
  public void attach()
  {
    super.attach();
    
    new ResultThread().start();
  }
  
  
  
  private class ResultThread extends Thread
  {

    @Override
    public void run()
    {
      long startTime = System.currentTimeMillis();
      AnnisService service = ServiceHelper.getService(getApplication(), getWindow());
      if(service != null)
      {
        try
        {
          service.getResultSet(new LinkedList<Long>(corpora), 
            aql, 0, 20, contextLeft, contextRight);
        }
        catch(RemoteException ex)
        {
          Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE, null, ex);
          getWindow().showNotification(ex.getLocalizedMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSemanticsException ex)
        {
          getWindow().showNotification("Sematic error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSyntaxException ex)
        {
          getWindow().showNotification("Syntax error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisCorpusAccessException ex)
        {
          getWindow().showNotification("Corpus access error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
      }
      long endTime = System.currentTimeMillis();
      
      piResult.setEnabled(false);
      
      lblInfo.setValue("Result for query \"" + aql.replaceAll("\n", " ") 
        + " finished in " + ((endTime - startTime) / 1000.0) + " seconds");
    }
    
    
  }
}
