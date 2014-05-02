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
package annis.gui.exporter;

import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.WebResource;
import java.io.Writer;
import java.util.Set;

/**
 *
 * @author thomas
 */
public interface Exporter
{
  public void convertText(String queryAnnisQL, int contextLeft, int contextRight, 
   Set<String> corpora, String keysAsString, String argsAsString, 
   WebResource annisResource, Writer out, EventBus eventBus);
  
  public boolean isCancelable();
  
}
