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
package annis.libgui.exporter;

import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.WebResource;

import annis.service.objects.CorpusConfig;
import net.xeoh.plugins.base.Plugin;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface ExporterPlugin extends Plugin
{
  public Exception convertText(String queryAnnisQL, int contextLeft, int contextRight, 
   Set<String> corpora, List<String> keys, String args, boolean alignmc,
   WebResource annisResource, Writer out, EventBus eventBus,
   Map<String, CorpusConfig> corpusConfigs);
  
  public boolean isCancelable();
  
  public boolean isAlignable();
  
  public String getHelpMessage();
  
  public String getFileEnding();
  
}
