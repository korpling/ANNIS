/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.CommonHelper;
import annis.gui.AnnisUI;
import annis.gui.MetaDataPanel;
import annis.libgui.IDGenerator;
import annis.model.DisplayedResultQuery;
import annis.service.objects.Match;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SCorpus;

/**
 *
 * @author thomas
 */
public class SingleCorpusResultPanel extends CssLayout {
    private static final long serialVersionUID = 2L;

    private static final Resource ICON_RESOURCE = FontAwesome.INFO_CIRCLE;

    private Collection<SCorpus> result;


    private final HorizontalLayout infoBar;


    public SingleCorpusResultPanel(final Collection<SCorpus> result, Match match, long resultNumber,
            AnnisUI ui, DisplayedResultQuery query) {
        this.result = result == null ? new LinkedList<>() : result;

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
        lblNumber.setSizeUndefined();

        VerticalLayout corpusPaths = new VerticalLayout();
        infoBar.addComponent(corpusPaths);
        infoBar.setExpandRatio(corpusPaths, 1.0f);

        for (SCorpus c : this.result) {
            List<String> path = CommonHelper.getCorpusPath(c.getPath().toString());

            HorizontalLayout corpusInfoLayout = new HorizontalLayout();
            corpusPaths.addComponent(corpusInfoLayout);

            // build label
            StringBuilder sb = new StringBuilder("Path: ");
            sb.append(StringUtils.join(path, " > "));

            Button btInfo;
            btInfo = new Button();
            btInfo.setStyleName(ValoTheme.BUTTON_BORDERLESS);
            btInfo.setIcon(ICON_RESOURCE);
            btInfo.setDescription("Show metadata");
            btInfo.addClickListener(event -> {
                if (event.getButton() == btInfo && result != null) {
                    Window infoWindow = new Window("Info for " + c.getId());

                    infoWindow.setModal(false);
                    MetaDataPanel meta = new MetaDataPanel(path.get(0), Optional.of(path.get(path.size() - 1)));
                    infoWindow.setContent(meta);
                    infoWindow.setWidth("400px");
                    infoWindow.setHeight("400px");

                    UI.getCurrent().addWindow(infoWindow);
                }

            });
            corpusInfoLayout.addComponent(btInfo);

            Label lblPath = new Label(sb.toString());
            lblPath.addStyleName("path-label");
            // lblPath.addStyleName(MatchedNodeColors.colorClassByMatch(matchIdx));

            lblPath.setWidth("100%");
            lblPath.setHeight("-1px");
            corpusInfoLayout.addComponent(lblPath);
        

        }

        infoBar.setSpacing(false);

        addComponent(infoBar);

        Label lblEmpty = new Label("Result matches only (sub-) corpora and their metadata. "
                + "You might want to extend your query to include a token search. <br/> "
                + "An example would be: <br/> <code>tok @* my_meta_attribute=\"somevalue\"</code>");
        lblEmpty.setContentMode(ContentMode.HTML);
        lblEmpty.addStyleName("empty_token_hint");

        addComponent(lblEmpty);
    }

    @Override
    public void attach() {
        super.attach();
        IDGenerator.assignIDForFields(SingleCorpusResultPanel.this, infoBar);
    }

}
