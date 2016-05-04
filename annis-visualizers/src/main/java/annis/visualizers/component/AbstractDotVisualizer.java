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
package annis.visualizers.component;

import annis.libgui.ImagePanel;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Embedded;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public abstract class AbstractDotVisualizer extends AbstractVisualizer<ImagePanel>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractDotVisualizer.class);
  
  @Override
  public ImagePanel createComponent(final VisualizerInput visInput, VisualizationToggle visToggle)
  { 
    try
    {
     
      final PipedOutputStream out = new PipedOutputStream();
      final PipedInputStream in = new PipedInputStream(out);
      
      
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          writeOutput(visInput, out);
        }
      }).start();

       String fileName = "dotvis_" 
        + new Random().nextInt(Integer.MAX_VALUE) + ".png";
      StreamResource resource = new StreamResource(new StreamResource.StreamSource()
        {

          @Override
          public InputStream getStream()
          {
            return in;
          }
        }, fileName);
      
      Embedded emb = new Embedded("", resource);
      emb.setMimeType("image/png");
      emb.setSizeFull();
      emb.setStandby("loading image");
      emb.setAlternateText("DOT graph visualization");
      return new ImagePanel(emb);

    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return new ImagePanel(new Embedded());
  }

  public void writeOutput(VisualizerInput input, OutputStream outstream)
  {
    
    StringBuilder dot = new StringBuilder();
   
    try
    {
      File tmpInput = File.createTempFile("annis-dot-input", ".dot");
      tmpInput.deleteOnExit();
      
      // write out input file
      StringBuilder dotContent = new StringBuilder();
      createDotContent(input, dotContent);
      FileUtils.writeStringToFile(tmpInput, dotContent.toString());
     
      // execute dot
      String dotPath = input.getMappings().getProperty("dotpath", "dot");
      ProcessBuilder pBuilder = new ProcessBuilder(dotPath,
          "-Tpng", 
        tmpInput.getCanonicalPath());
     
      
      pBuilder.redirectErrorStream(false);
      Process process = pBuilder.start();
     
      try (InputStream inputFromProcess = process.getInputStream())
      {
        for(int chr=inputFromProcess.read(); chr != -1; chr = inputFromProcess.read())
        {
          outstream.write(chr);
        }
      }
      
      int resultCode = process.waitFor();
      
      if(resultCode != 0)
      {  
        InputStream stderr = process.getErrorStream();
        StringBuilder errorMessage = new StringBuilder();
        
        for(int chr=stderr.read(); chr != -1; chr = stderr.read())
        {
          errorMessage.append((char) chr);
        }
        if (!"".equals(errorMessage.toString()))
        {
          log.error(
            "Could not execute dot graph-layouter.\ncommand line:\n{}\n\nstderr:\n{}\n\nstdin:\n{}",
            new Object[]
            {
              StringUtils.join(pBuilder.command(), " "), 
              errorMessage.toString(), dot.toString()
            });
        }
      }
      
      
      // cleanup
      if(!tmpInput.delete())
      {
        log.warn("Cannot delete " + tmpInput.getAbsolutePath());
      }

    }
    catch (IOException | InterruptedException ex)
    {
      log.error(null, ex);
    }

  }

  public abstract void createDotContent(VisualizerInput input, StringBuilder sb);
}
