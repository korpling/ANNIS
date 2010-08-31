/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.frontend.servlets.visualizers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public abstract class AbstractDotVisualizer extends WriterVisualizer
{

  public final int scale = 50;

  @Override
  public final void writeOutput(Writer writer)
  {
   
    StringBuilder dot = new StringBuilder();

    try
    {
      String cmd = getMappings().getProperty("dotpath", "dot") + " -s" + scale + ".0 -Tpng";
      Runtime runTime = Runtime.getRuntime();
      Process p = runTime.exec(cmd);
      OutputStreamWriter stdin = new OutputStreamWriter(p.getOutputStream(), "UTF-8");

      createDotContent(dot);
      
      Logger.getLogger(AbstractDotVisualizer.class.getName()).log(Level.FINE,
        "outputting dot graph:\n{0}", dot.toString());

      stdin.append(dot);
      stdin.flush();

      p.getOutputStream().close();
      int chr;
      InputStream stdout = p.getInputStream();
      StringBuilder outMessage = new StringBuilder();
      while ((chr = stdout.read()) != -1)
      {
        writer.write(chr);
        outMessage.append((char) chr);
      }

      StringBuilder errorMessage = new StringBuilder();
      InputStream stderr = p.getErrorStream();
      while ((chr = stderr.read()) != -1)
      {
        errorMessage.append((char) chr);
      }

      p.destroy();
      writer.flush();

      if (!"".equals(errorMessage.toString()))
      {
        Logger.getLogger(AbstractDotVisualizer.class.getName()).log(
          Level.SEVERE,
          "Could not execute dot graph-layouter.\ncommand line:\n{0}\n\nstderr:\n{1}\n\nstdin:\n{2}",
          new Object[]
          {
            cmd, errorMessage.toString(), dot.toString()
          });
      }

    }
    catch (IOException ex)
    {
      Logger.getLogger(AbstractDotVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
  
  public abstract void createDotContent(StringBuilder sb);

  @Override
  public String getContentType()
  {
    return "image/png";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "ISO-8859-1";
  }
}
