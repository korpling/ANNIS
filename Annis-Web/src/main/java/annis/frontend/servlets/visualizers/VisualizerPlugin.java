/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
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
package annis.frontend.servlets.visualizers;

import java.io.OutputStream;
import net.xeoh.plugins.base.Plugin;

/**
 * Base interface for all visualizer plugins
 * @author Thomas Krause <krause@informatik.hu-berlin.>
 */
public interface VisualizerPlugin extends Plugin
{

  /**
   * Writes the final output to passed OutputStream. The stream should remain open.
   * 
   * @param input The input from which the visualization should be generated from
   * @param outstream The OutputStream to be used
   */
  public void writeOutput(VisualizerInput input, OutputStream outstream);

  /**
   * Returns the character endocing for this particular Visualizer output. For more information see {@link javax.servlet.ServletResponse#setCharacterEncoding(String)}.
   * Must be overridden to change default "utf-8".
   * @return the CharacterEncoding
   */
  public String getCharacterEncoding();

  /**
   * Returns the content-type for this particular Visualizer output. For more information see {@link javax.servlet.ServletResponse#setContentType(String)}.
   * Must be overridden to change default "text/html".
   * @return the ContentType
   */
  public String getContentType();
}
