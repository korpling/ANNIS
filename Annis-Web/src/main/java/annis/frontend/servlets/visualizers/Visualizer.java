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
package annis.frontend.servlets.visualizers;

import annis.service.ifaces.AnnisResult;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public abstract class Visualizer implements VisualizerPlugin
{

  @Override
  public abstract void writeOutput(VisualizerInput input, OutputStream outstream);

  @Override
  public String getContentType()
  {
    return "text/html";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "utf-8";
  }


}
