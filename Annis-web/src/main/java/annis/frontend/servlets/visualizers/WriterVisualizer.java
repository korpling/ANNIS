/*
 * Copyright 2009 Collaborative Research Centre SFB 632
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Implentation of Visualizer which uses Writers instead of OutputStream
 * @author thomas
 */
public abstract class WriterVisualizer extends Visualizer
{

  /**
   * Will create a Writer of the outstream.
   * @param outstream
   */
  @Override
  public void writeOutput(OutputStream outstream)
  {
    try
    {
      OutputStreamWriter writer = new OutputStreamWriter(outstream, getCharacterEncoding());
      writeOutput(writer);
      writer.flush();
    }
    catch(IOException ex)
    {
      ex.printStackTrace(new PrintWriter(outstream));
    }
  }

  /**
   * Writes the final output to passed Wriiter. The writer should remain open.
   * @param writer
   */
  public abstract void writeOutput(Writer writer);

}
