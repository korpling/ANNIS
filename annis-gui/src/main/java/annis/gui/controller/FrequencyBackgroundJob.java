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
package annis.gui.controller;

import annis.gui.frequency.FrequencyQueryPanel;
import annis.gui.objects.FrequencyQuery;
import annis.libgui.Helper;
import annis.service.objects.FrequencyTable;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyBackgroundJob implements Callable<FrequencyTable>
{
  private static final Logger log = LoggerFactory.getLogger(
    FrequencyBackgroundJob.class);
  
  private final UI ui;

  private final FrequencyQuery query;

  private final FrequencyQueryPanel panel;

  public FrequencyBackgroundJob(UI ui, FrequencyQuery query,
    FrequencyQueryPanel panel)
  {
    this.ui = ui;
    this.query = query;
    this.panel = panel;
  }

  @Override
  public FrequencyTable call() throws Exception
  {
    final FrequencyTable t = loadBeans();
    ui.access(new Runnable()
    {
      @Override
      public void run()
      {
        panel.showResult(t, query);
      }
    });
    return t;
  }

  private FrequencyTable loadBeans()
  {
    FrequencyTable result = new FrequencyTable();
    WebResource annisResource = Helper.getAnnisWebResource();
    try
    {
      annisResource = annisResource.path("query").path("search").
        path("frequency").queryParam("q", query.getQuery()).
        queryParam("corpora", StringUtils.join(query.getCorpora(), ",")).
        queryParam("fields", query.getFrequencyDefinition().toString());
      result = annisResource.get(FrequencyTable.class);
    }
    catch (UniformInterfaceException ex)
    {
      String message;
      if (ex.getResponse().getStatus() == 400)
      {
        message = ex.getResponse().getEntity(String.class);
      }
      else if (ex.getResponse().getStatus() == 504)
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
      log.error("could not execute REST call to query frequency",
        ex);
    }
    return result;
  }
  
}
