/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.visualizers.component.graph;

import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.AbstractDotVisualizer;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.io.FileUtils;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class DebugVisualizer extends AbstractDotVisualizer implements Serializable
{

  private final static Logger log = LoggerFactory.getLogger(DebugVisualizer.class);
  
  @Override
  public void createDotContent(VisualizerInput input, StringBuilder sb)
  {
    try
    {
      File tmpFile = File.createTempFile("annisdebugvis", ".dot");
      tmpFile.deleteOnExit();
      SaltUtil.save_DOT(input.getDocument().getDocumentGraph(), URI.createFileURI(tmpFile.getCanonicalPath()));
      
      sb.append(FileUtils.readFileToString(tmpFile));
      
      if(!tmpFile.delete())
      {
        log.warn("Cannot delete " + tmpFile.getAbsolutePath());
      }
    }
    catch (IOException ex)
    {
      log.error("could not create temporary file for dot", ex);
    }
  }

  @Override
  public String getShortName()
  {
    return "dot";
  }
  
}
