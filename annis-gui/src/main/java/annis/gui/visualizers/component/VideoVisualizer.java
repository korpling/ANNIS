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
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.VideoPlayer;
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class VideoVisualizer extends AbstractVisualizer<VideoPlayer>
{

  private Logger log = LoggerFactory.getLogger(VideoVisualizer.class);
  
  @InjectPlugin
  public MediaControllerFactory mcFactory;

  @Override
  public String getShortName()
  {
    return "video";
  }

  @Override
  public VideoPlayer createComponent(VisualizerInput input, Application application)
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
    
    WebResource resMeta = Helper.getAnnisWebResource(application).path(
      "query/corpora").path(corpusName).path(documentName).path("/binary/meta");
    List<AnnisBinaryMetaData> meta = resMeta.get(new GenericType<List<AnnisBinaryMetaData>>() {});

    String mimeType = null;
    for(AnnisBinaryMetaData m : meta)
    {
      if(m.getMimeType().startsWith("video/"))
      {
        mimeType = m.getMimeType();
        break;
      }
    }
    
    Validate.notNull(mimeType, "There must be at least one binary file for the document with a video mime type");
    
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
      + "&mime=" + mimeTypeEncoded;
    
    VideoPlayer player = new VideoPlayer(binaryServletPath, mimeType);

    if (mcFactory != null && application instanceof MediaControllerHolder)
    {
      mcFactory.getOrCreate((MediaControllerHolder) application)
        .addMediaPlayer(player, input.getId(), input.getVisPanel());
    }

    return player;
  }
}
