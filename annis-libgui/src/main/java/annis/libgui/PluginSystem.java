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
package annis.libgui;

import annis.libgui.exporter.ExporterPlugin;
import annis.libgui.visualizers.VisualizerPlugin;
import java.io.Serializable;
import net.xeoh.plugins.base.PluginManager;

/**
 * ANNIS plugin system interface for getting plugins or specific 
 * visualizers.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface PluginSystem extends Serializable
{
  
  public final static String DEFAULT_VISUALIZER = "grid";
  
  /**
   *  Get the JSPF Plugin Manager 
   * @return plugin manager
   */
  public PluginManager getPluginManager();
  
  /** 
   * Gets a visualizer by its short name 
   * @param shortName short name of the visualizer
   * @return visualizer
   */
  public VisualizerPlugin getVisualizer(String shortName); 
  
  /** 
   * Gets the plugin instance of an exporter by it's class 
   * @param clazz class of the exporter
   * @return exporter
   */
  public ExporterPlugin getExporter(Class<? extends ExporterPlugin> clazz);
  
}
