/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.resultview;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.MetaDataPanel;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.objects.Match;

public class SingleCorpusResultPanel extends CssLayout {
  private static final long serialVersionUID = 3L;

  private static final Resource ICON_RESOURCE = VaadinIcons.INFO_CIRCLE;

  private final HorizontalLayout infoBar;


  public SingleCorpusResultPanel(final Collection<String> matchedPaths, Match match,
      long resultNumber,
      AnnisUI ui, DisplayedResultQuery query) {

    setWidth("100%");
    setHeight("-1px");

    if (query != null && query.getSelectedMatches().contains(resultNumber)) {
      addStyleName("selected-match");
    }

    infoBar = new HorizontalLayout();
    infoBar.addStyleName("info-bar");
    infoBar.addStyleName("no-document-info-bar");
    infoBar.setWidth("100%");
    infoBar.setHeight("-1px");

    Label lblNumber = new Label("" + (resultNumber + 1));
    infoBar.addComponent(lblNumber);

    for (String m : matchedPaths) {
      List<String> path = Helper.getCorpusPath(m);

      // build label
      StringBuilder sb = new StringBuilder("Path: ");
      sb.append(StringUtils.join(path, " > "));

      if (!path.isEmpty()) {
        Button btInfo;
        btInfo = new Button();
        btInfo.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        btInfo.setIcon(ICON_RESOURCE);
        btInfo.setDescription("Show metadata");
        btInfo.addClickListener(event -> {
          Window infoWindow = new Window("Info for " + path.get(path.size() - 1));
          infoWindow.setModal(false);
          Optional<String> documentName =
              path.size() > 1 ? Optional.of(path.get(path.size() - 1)) : Optional.empty();
          MetaDataPanel meta =
              new MetaDataPanel(path.get(0), documentName);
          infoWindow.setContent(meta);
          infoWindow.setWidth("400px");
          infoWindow.setHeight("400px");

          UI.getCurrent().addWindow(infoWindow);


        });
        infoBar.addComponent(btInfo);
      }

      Label lblPath = new Label(sb.toString());
      lblPath.addStyleName("path-label");

      infoBar.addComponent(lblPath);
      infoBar.setExpandRatio(lblPath, 1.0f);
      infoBar.setSpacing(false);
    }

    infoBar.setSpacing(false);

    addComponent(infoBar);

    Label lblEmpty = new Label(
        "Result matches only (sub-) corpora/documents and their metadata directly, but not any annotation that can be visualized. "
            + "You might want to extend your query to include a token search. <br/> "
            + "An example would be: <br/> <code>tok @* my_meta_attribute=\"somevalue\"</code>");
    lblEmpty.addStyleName("empty_token_hint");
    lblEmpty.setContentMode(ContentMode.HTML);

    addComponent(lblEmpty);
  }

}
