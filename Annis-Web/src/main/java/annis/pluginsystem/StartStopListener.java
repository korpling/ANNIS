/*
 * Copyright 2011 Collaborative Research Centre SFB 632 
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
package annis.pluginsystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 *
 * @author thomas
 */
public class StartStopListener implements ServletContextListener
{

  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    PluginManager pm = PluginManagerFactory.createPluginManager();
    URI classpath = ClassURI.CLASSPATH("annis.plugins.**");
    if(classpath != null)
    {
      pm.addPluginsFrom(classpath);
    }
    try
    {
      URL basicPlugins = sce.getServletContext().getResource("plugins");
      if(basicPlugins != null)
      {
        pm.addPluginsFrom(basicPlugins.toURI());
      }
    }
    catch(MalformedURLException ex)
    {
      Logger.getLogger(StartStopListener.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch(URISyntaxException ex)
    {
      Logger.getLogger(StartStopListener.class.getName()).log(Level.SEVERE, null, ex);
    }

    String globalPlugins = System.getenv("ANNIS_PLUGINS");
    if(globalPlugins != null)
    {
      pm.addPluginsFrom(new File(globalPlugins).toURI());
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
  }
}
