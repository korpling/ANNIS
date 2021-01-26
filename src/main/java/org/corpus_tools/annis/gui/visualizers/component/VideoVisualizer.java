/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import java.util.List;
import org.apache.tika.Tika;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.components.medialement.MediaElement;
import org.corpus_tools.annis.gui.components.medialement.MediaElementPlayer;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class VideoVisualizer extends AbstractVisualizer { // NO_UCD (unused code)

  /**
   * 
   */
  private static final long serialVersionUID = -5520916972367860787L;
  private final static Escaper urlParamEscape = UrlEscapers.urlPathSegmentEscaper();

  private final Tika tika = new Tika();

  @Override
  public MediaElementPlayer createComponent(VisualizerInput input, VisualizationToggle visToggle) {
    List<String> corpusPath =
        Helper.getCorpusPath(input.getDocument().getGraph(), input.getDocument());

    String binaryServletPath = "";
    String mimeType = null;

    String corpusName = corpusPath.get(corpusPath.size() - 1);

    CorporaApi api = new CorporaApi(Helper.getClient(input.getUI()));
    try {
      List<String> files =
          api.listFiles(corpusName, Joiner.on('/').join(Lists.reverse(corpusPath)));
      for (String f : files) {
        String guessedMimeType = tika.detect(f);
        if (guessedMimeType != null && guessedMimeType.startsWith("video/")) {
          binaryServletPath =
              input.getContextPath() + "/Binary?" + "file=" + urlParamEscape.escape(f)
                  + "&toplevelCorpusName=" + urlParamEscape.escape(corpusName);
          mimeType = guessedMimeType;
        }
      }
    } catch (ApiException e) {
      ExceptionDialog.show(e, UI.getCurrent());
    }


    MediaElementPlayer player =
        new MediaElementPlayer(MediaElement.video, binaryServletPath, mimeType);

    if (VaadinSession.getCurrent().getAttribute(MediaController.class) != null) {
      VaadinSession.getCurrent().getAttribute(MediaController.class).addMediaPlayer(player,
          input.getId(), visToggle);
    }

    return player;
  }

  @Override
  public String getShortName() {
    return "video";
  }

}
