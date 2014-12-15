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
package annis.gui;

import annis.gui.components.ExceptionDialog;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.objects.QueryUIState;
import annis.libgui.Helper;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Property;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A controller to modifiy the query UI state.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryController implements Serializable
{
  private static final Logger log = LoggerFactory.getLogger(QueryController.class);
  
  private final SearchUI ui;
  
  private final QueryUIState state;

  public QueryController(SearchUI ui)
  {
    this.ui = ui;
    this.state = ui.getQueryState();
    
    this.state.getAql().addValueChangeListener(new Property.ValueChangeListener()
    {

      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        validateQuery(QueryController.this.state.getAql().getValue());
      }
    });
  }
  
  
  private void validateQuery(String query)
  {
    QueryPanel qp = ui.getControlPanel().getQueryPanel();
    if(query == null || query.isEmpty())
    {
      qp.setStatus("Empty query");
    }
    else
    {
      // validate query
      try
      {
        AsyncWebResource annisResource = Helper.getAnnisAsyncWebResource();
        Future<String> future = annisResource.path("query").path("check").queryParam("q", query)
          .get(String.class);

        // wait for maximal one seconds

        try
        {
          String result = future.get(1, TimeUnit.SECONDS);

          if ("ok".equalsIgnoreCase(result))
          {
            if(state.getSelectedCorpora().getValue().isEmpty())
            {
              qp.setStatus("Please select a corpus from the list below, then click on \"Search\".");
            }
            else
            {
              qp.setStatus("Valid query, click on \"Search\" to start searching.");
            }
          }
          else
          {
            qp.setStatus(result);
          }
        }
        catch (InterruptedException ex)
        {
          log.warn(null, ex);
        }
        catch (ExecutionException ex)
        {
          if(ex.getCause() instanceof UniformInterfaceException)
          {
            UniformInterfaceException cause = (UniformInterfaceException) ex.
              getCause();
            if (cause.getResponse().getStatus() == 400)
            {
              qp.setStatus(cause.getResponse().getEntity(String.class));
            }
            else
            {
              log.error(
                "Exception when communicating with service", ex);
              ExceptionDialog.show(ex,
                "Exception when communicating with service.");
            }
          }
        }
        catch (TimeoutException ex)
        {
          qp.setStatus("Validation of query took too long.");
        }

      }
      catch(ClientHandlerException ex)
      {
        log.error(
            "Could not connect to web service", ex);
          ExceptionDialog.show(ex, "Could not connect to web service");
      }
    }
  }
  
}
