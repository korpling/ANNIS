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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import annis.libgui.Helper;
import annis.libgui.exporter.ExporterPlugin;
import annis.service.objects.CorpusConfig;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class WekaExporter implements ExporterPlugin, Serializable
{

  @Override
  public Exception convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, List<String> keys, String argsAsString, boolean alignmc,
    WebResource annisResource, Writer out, EventBus eventBus, Map<String, CorpusConfig> corpusConfigs)
  {
    //this is a full result export
    
    try
    {
      WebResource res = annisResource.path("search").path("matrix")
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .queryParam("q", Helper.encodeJersey(queryAnnisQL));
      
      
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
      
      return null;
    }
    catch(UniformInterfaceException | ClientHandlerException | IOException ex)
    {
      return ex;
    }
  }

  @Override
  public boolean isCancelable()
  {
    return false;
  }
  
  @Override
  public String getHelpMessage()
  {
    return  "The WEKA Exporter exports only the "
        + "values of the elements searched for by the user, ignoring the context "
        + "around search results. The values for all annotations of each of the "
        + "found nodes is given in a comma-separated table (CSV). At the top of "
        + "the export, the names of the columns are given in order according to "
        + "the WEKA format.<br/><br/>"
        + "Parameters: <br/>"
        + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
        + "<code>metakeys=title,documentname</code>)";
  }
  
  @Override
  public String getFileEnding()
  {
    return "arff";
  }

@Override
public boolean isAlignable() 
 {
	
	return false;
 }
  
}
