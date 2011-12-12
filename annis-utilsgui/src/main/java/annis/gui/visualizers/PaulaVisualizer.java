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
package annis.gui.visualizers;

import java.io.IOException;
import java.io.Writer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class PaulaVisualizer extends WriterVisualizer
{

  @Override
  public String getShortName()
  {
    return "paula";
  }

  
  
  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {
      writer.append("<html><head><style> body { font-family: verdana, arial; font-size: 10px; } </style><body>");
      writer.append(input.getPaula().replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</body></html>");
    }
    catch(IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
