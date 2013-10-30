/*
 * Copyright 2013 SFB 632.
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

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.RawTextWrapper;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import java.util.regex.Pattern;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Renders the plain text from the text table.
 *
 * <p>
 * Therefore this visualizer is faster, than a visualizer which has to traverse
 * a {@link SaltProject} for getting the spanned {@link STextualDS}.</p>
 *
 * <p>
 * <strong>Mappings</strong>: <code>vertical: true | false</code> - Defines the
 * alignment of multiple text, e. g. for parralel corpora it is convenient
 * way.</p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class RawTextVisualizer extends AbstractVisualizer<Panel> {

    private final String NO_TEXT = "no text available";

    // pattern for checking the token layer
    private final Pattern whiteSpaceMatcher = Pattern.compile("^\\s+$");

    @Override
    public String getShortName() {
        return "raw_text";
    }

    @Override
    public boolean isUsingRawText() {
        return true;
    }

    @Override
    public Panel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {

        // get config for alignment
        boolean vertical = Boolean.parseBoolean(visInput.getMappings().getProperty("vertical", "true"));

        // get the texts
        RawTextWrapper texts = visInput.getRawText();

        // create the main panel
        Panel p = new Panel();
        p.setSizeFull();

        Layout l;

        if (texts == null) {
            Label text = new Label(NO_TEXT);
            text.setSizeFull();
            p.setContent(text);
            return p;
        }

        if (texts.hasMultipleTexts()) {

            // set the aligmnent
            if (vertical) {
                l = new VerticalLayout();
            } else {
                l = new HorizontalLayout();
            }

            // limit the size to the parent panel.
            l.setSizeFull();

            // add the texts to the layout
            for (int i = 0; i < texts.getTexts().size(); i++) {

                String s = texts.getTexts().get(i);
                Label lblText;

                // check if the text is empty
                if (s == null || hasOnlyWhiteSpace(s)) {
                    lblText = new Label(NO_TEXT);
                } else {
                    lblText = new Label(s, ContentMode.TEXT);
                }

                lblText.setCaption("text " + (i + 1));
                l.addComponent(lblText);
            }

            // apply the panel
            p.setContent(l);
            return p;

        }

        Label lblText;
        if (texts.hasTexts() && !hasOnlyWhiteSpace(texts.getFirstText())) {
            lblText = new Label(texts.getFirstText(), ContentMode.TEXT);
        } else {
            lblText = new Label(NO_TEXT);
        }

        lblText.setSizeFull();
        p.setContent(lblText);

        return p;
    }

    /**
     * Checks whether a string contains only whitespace. This is the cases for
     * corpora with an artificial token layer.
     *
     * @param text The text which is checked. Throws
     * {@link NullPointerException} when text is empty.
     *
     * @return true if only whitespace in there.
     */
    public boolean hasOnlyWhiteSpace(String text) {
        return whiteSpaceMatcher.matcher(text).matches();
    }

}
