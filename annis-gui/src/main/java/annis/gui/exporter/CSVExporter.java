/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.exporter;

import annis.libgui.Helper;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class CSVExporter implements Exporter, Serializable
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CSVExporter.class);

  @Override
  public boolean convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, List<String> keys,String argsAsString,
    WebResource annisResource, Writer out, EventBus eventBus)
  {
    //this is a full result export
    
    try
    {
      WebResource res = annisResource.path("search").path("matrix")
        .queryParam("csv", "true")
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .queryParam("q", Helper.encodeTemplate(queryAnnisQL));
      
      
      if(argsAsString.startsWith("metakeys="))
      {
        res = res.queryParam("metakeys", argsAsString.substring("metakeys".length()+1));
      }
      
      try
      (InputStream result = res.get(InputStream.class)) 
      {
        IOUtils.copy(result, out);
      }
      
      out.flush();
      
      return true;
    }
    catch(UniformInterfaceException ex)
    {
      log.error(null, ex);
      Notification n = new Notification("Service exception", ex.getResponse().getEntity(String.class),
        Notification.Type.WARNING_MESSAGE, true);
      n.show(Page.getCurrent());
    }
    catch(ClientHandlerException ex)
    {
      log.error(null, ex);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return false;
  }

  @Override
  public boolean isCancelable()
  {
    return false;
  }

  @Override
  public String getHelpMessage()
  {
	return "The CSV Exporter exports only the "
			+ "values of the elements searched for by the user, ignoring the context "
			+ "around search results. The values for all annotations of each of the "
			+ "found nodes is given in a comma-separated table (CSV). <br/><br/>"
			+ "Parameters: <br/>"
			+ "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
			+ "<code>metakeys=title,documentname</code>)";
  }
}
