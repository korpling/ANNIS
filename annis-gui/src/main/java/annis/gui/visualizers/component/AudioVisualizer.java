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
package annis.gui.visualizers.component;

import annis.CommonHelper;
import annis.gui.Helper;
import annis.gui.VisualizationToggle;
import annis.gui.media.MediaController;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.AudioPlayer;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class AudioVisualizer extends AbstractVisualizer<AudioPlayer>
{

  private Logger log = LoggerFactory.getLogger(AudioVisualizer.class);
  
  @Override
  public String getShortName()
  {
    return "audio";
  }

  @Override
  public AudioPlayer createComponent(VisualizerInput input, VisualizationToggle visToggle)
  {
    List<String> corpusPath =
      CommonHelper.getCorpusPath(input.getDocument().getSCorpusGraph(), input.getDocument());

    String binaryServletPath = "";

    String corpusName = corpusPath.get(corpusPath.size() - 1);
    String documentName = corpusPath.get(0);
    try
    {
      corpusName = URLEncoder.encode(corpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error("UTF-8 was not known as encoding, expect non-working audio", ex);
    }
    
    WebResource resMeta = Helper.getAnnisWebResource().path(
      "query/corpora").path(corpusName).path(documentName).path("/binary/meta");
    List<AnnisBinaryMetaData> meta = resMeta.get(new GenericType<List<AnnisBinaryMetaData>>() {});

    // if there is no document at all don't fail
    String mimeType = meta.size() > 0 ? null : "audio/ogg";
    for(AnnisBinaryMetaData m : meta)
    {
      if(m.getMimeType().startsWith("audio/"))
      {
        mimeType = m.getMimeType();
        break;
      }
    }
    
    Validate.notNull(mimeType, "There must be at least one binary file for the document with a audio mime type");
    
    String mimeTypeEncoded = mimeType;
    try
    {
      mimeTypeEncoded = URLEncoder.encode(mimeType, "UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error("UTF-8 was not known as encoding, expect non-working audio", ex);
    }
    
    binaryServletPath = input.getContextPath() + "/Binary?"
      + "documentName=" + documentName
      + "&toplevelCorpusName=" + corpusName
      + "&mime=" +  mimeTypeEncoded;      

    AudioPlayer player = new AudioPlayer(binaryServletPath, mimeType);

    if (VaadinSession.getCurrent().getAttribute(MediaController.class) != null)
    {  
      VaadinSession.getCurrent().getAttribute(MediaController.class)
        .addMediaPlayer(player, input.getId(), visToggle);
    }

    return player;
  }
}
