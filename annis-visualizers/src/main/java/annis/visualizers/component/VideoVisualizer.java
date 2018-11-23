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
package annis.visualizers.component;

import java.util.List;

import org.apache.commons.lang3.Validate;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;

import annis.CommonHelper;
import annis.gui.components.medialement.MediaElement;
import annis.gui.components.medialement.MediaElementPlayer;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.AnnisBinaryMetaData;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class VideoVisualizer extends AbstractVisualizer<MediaElementPlayer>
{
  
  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();
  private final static Escaper urlParamEscape = UrlEscapers.urlPathSegmentEscaper();
  
  @Override
  public String getShortName()
  {
    return "video";
  }

  @Override
  public MediaElementPlayer createComponent(VisualizerInput input, VisualizationToggle visToggle)
  {
    List<String> corpusPath =
      CommonHelper.getCorpusPath(input.getDocument().getGraph(), input.getDocument());

    String binaryServletPath = "";

    String corpusName = corpusPath.get(corpusPath.size() - 1);
    String documentName = corpusPath.get(0);
    corpusName = urlPathEscape.escape(corpusName);
    documentName = urlPathEscape.escape(documentName);
    
    WebResource resMeta = Helper.getAnnisWebResource().path(
      "meta/binary").path(corpusName).path(documentName);
    List<AnnisBinaryMetaData> meta = resMeta.get(new GenericType<List<AnnisBinaryMetaData>>() {});

    // if there is no document at all don't fail
    String mimeType = meta.size() > 0 ? null : "video/webm";
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
    mimeTypeEncoded = urlParamEscape.escape(mimeType);
   
    
    binaryServletPath = input.getContextPath() + "/Binary?"
      + "documentName=" + documentName
      + "&toplevelCorpusName=" + corpusName
      + "&mime=" + mimeTypeEncoded;
    
    MediaElementPlayer player = new MediaElementPlayer(MediaElement.video,
      binaryServletPath, mimeType);
    
    if (VaadinSession.getCurrent().getAttribute(MediaController.class) != null)
    {
      VaadinSession.getCurrent().getAttribute(MediaController.class)
        .addMediaPlayer(player, input.getId(), visToggle);
    }

    return player;
  }
}
