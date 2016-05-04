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
package annis.visualizers.iframe;

import annis.libgui.visualizers.AbstractIFrameVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.slf4j.LoggerFactory;

/**
 * Implentation of Visualizer which uses Writers instead of OutputStream
 * @author thomas
 */
public abstract class WriterVisualizer extends AbstractIFrameVisualizer
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(WriterVisualizer.class);
  
  /**
   * Will create a Writer of the outstream.
   * @param outstream
   */
  @Override
  public void writeOutput(VisualizerInput input, OutputStream outstream)
  {
    try
    {
      OutputStreamWriter writer = new OutputStreamWriter(outstream, getCharacterEncoding());
      writeOutput(input, writer);
      writer.flush();
    }
    catch(IOException ex)
    {
      log.error("Exception when writing visualizer output.", ex);
      StringWriter strWriter = new StringWriter();
      ex.printStackTrace(new PrintWriter(strWriter));
      try
      {
        outstream.write(strWriter.toString().getBytes("UTF-8"));
      }
      catch (IOException ex1)
      {
        log.error(null, ex);
      }
    }
  }

  /**
   * Writes the final output to passed Wriiter. The writer should remain open.
   * @param writer
   */
  public abstract void writeOutput(VisualizerInput input, Writer writer);

}
