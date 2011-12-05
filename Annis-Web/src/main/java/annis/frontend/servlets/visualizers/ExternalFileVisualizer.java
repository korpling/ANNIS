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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisBinary;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Visualizer for external files.
 * 
 * Currently it can files with the mime type "audio/mpeg" and "video/x-flv"
 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 */
@PluginImplementation
public class ExternalFileVisualizer extends WriterVisualizer
{

  private long externalID = -1;
  private VisualizerInput theInput;

  @Override
  public String getShortName()
  {
    return "file";
  }

  
  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    externalID = -1;
    this.theInput = input;
    
    for (AnnisNode n : theInput.getResult().getGraph().getNodes())
    {
      if (externalID < 0)
      {
        for (Annotation a : n.getNodeAnnotations())
        {
          if (externalID < 0 && a.getQualifiedName().equalsIgnoreCase("external:externalFile"))
          {
            try
            {
              externalID = Long.parseLong(a.getValue());
              break;
            }
            catch (NumberFormatException ex)
            {
            }
          }
        }
      }
    }

    if(externalID > -1)
    {
      try
      {
        AnnisService service = AnnisServiceFactory.getClient(theInput.getAnnisRemoteServiceURL());
        AnnisBinary binary = service.getBinary(externalID);

        if("audio/mpeg".equalsIgnoreCase(binary.getMimeType()))
        {
          writer.write(generateAudio());
        }
        else if("video/x-flv".equalsIgnoreCase(binary.getMimeType()))
        {
          writer.write(generateVideo());
        }
        else
        {
          writer.write("unknown mime-type \"" + binary.getMimeType() + "\"");
        }

      }
      catch (Exception ex)
      {
        Logger.getLogger(ExternalFileVisualizer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private String generateAudio()
  {
    String template =
        "<object type=\"application/x-shockwave-flash\" data=\"%s/mediaplayer/player_mp3_maxi.swf\" width=\"200\" height=\"20\">\n"
       + "<param name=\"movie\" value=\"%s/mediaplayer/player_mp3_maxi.swf\" />\n"
       + "<param name=\"bgcolor\" value=\"#ffffff\" />\n"
       + "<param name=\"FlashVars\" value=\"mp3="
       + "%s/secure/Externalfile/%d"
        + "&amp;width=85&amp;showstop=1&amp;showvolume=1&amp;showslider=0&amp;bgcolor1=dfe8f6&amp;bgcolor2=bad0ee&amp;sliderovercolor=ffffff&amp;buttoncolor=000000&amp;buttonovercolor=ffffff&amp;textcolor=000000\" />\n"
       + "</object>";

    return String.format(template, theInput.getContextPath(), 
      theInput.getContextPath(),
      theInput.getContextPath(), externalID);
  }

  private String generateVideo()
  {
    String template =
        "<object type=\"application/x-shockwave-flash\" data=\"%s/mediaplayer/player_flv_maxi.swf\" width=\"320\" height=\"240\">\n"
       + "<param name=\"movie\" value=\"%s/mediaplayer/player_flv_maxi.swf\" />\n"
       + "<param name=\"bgcolor\" value=\"#ffffff\" />\n"
       + "<param name=\"FlashVars\" value=\"flv="
        + "%s/secure/Binary/%d"
        + "&amp;showstop=1&amp;showvolume=1&amp;showtime=1&amp;showfullscreen=1\" />\n"
       + "</object>";

    return String.format(template, theInput.getContextPath(), 
      theInput.getContextPath(),
      theInput.getContextPath(), externalID);
  }

}

