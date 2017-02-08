/*
 * Copyright 2017 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.requesthandler;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Charsets;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Creates a single HTML page which lists all available corpora.
 *
 * @author Thomas Krause <thomaskrause@posteo.de>
 */
public class HTMLCorpusList implements RequestHandler
{
  
   private final String prefix;
  
   public HTMLCorpusList(String urlPrefix)
  {
    this.prefix = urlPrefix + "/corpus-list";
  }

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if (request.getPathInfo() != null && request.getPathInfo().
      startsWith(prefix))
    {
      HashMap<String, Object> scopes = new HashMap<String, Object>();

      MustacheFactory mustacheFactory = new DefaultMustacheFactory();
      
      try(InputStream is = getClass().getResourceAsStream("corpuslist.html"))
      {
        Mustache m = mustacheFactory.compile(new InputStreamReader(is, Charsets.UTF_8) , "corpuslist");
        m.execute(response.getWriter(), scopes);

        return true;
      }
    }
    return false;
  }

}
