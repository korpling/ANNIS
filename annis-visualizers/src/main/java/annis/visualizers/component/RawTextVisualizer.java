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

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.RawTextWrapper;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.regex.Pattern;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SaltProject;

/**
 * Renders the plain text from the text table.
 *
 * <p>
 * Therefore this visualizer is faster, than a visualizer which has to traverse
 * a {@link SaltProject} for getting the spanned {@link STextualDS}.</p>
 *
 * <p>
 * <strong>Mappings</strong>: <code>vertical: true | false</code> - Defines the
 * alignment of multiple text, e. g. for parralel corpora it is a convenient
 * visualization<./p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class RawTextVisualizer extends AbstractVisualizer<Panel> {
    
    private static final String NO_TEXT = "no text available";

    // pattern for checking the token layer
    private final Pattern whiteSpaceMatcher = Pattern.compile("^\\s+$");

    // 
    private static final String PANEL_CLASS = "raw_text";
    
    private static final String LABEL_CLASS = "raw_text_label";
    
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

        // some layout configuration
        p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
        p.addStyleName(PANEL_CLASS);
        
        // enable webfonts
        p.addStyleName(Helper.CORPUS_FONT_FORCE);

        Layout l;
        
        // if no text available inform user and exit
        if (texts == null) {
            Label text = new Label(NO_TEXT);
            text.addStyleName(LABEL_CLASS);
            text.setSizeFull();
            p.setContent(text);
            return p;
        }
        
        if (texts.hasMultipleTexts()) {

            // set the aligmnent
            if (vertical) {
                l = new VerticalLayout();
            } else {
                l = new GridLayout(texts.getTexts().size(), 1);
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
                
                if(!Helper.isRTLDisabled() && CommonHelper.containsRTLText(s))
                {
                  lblText.addStyleName("rtl");
                }
                
                lblText.setCaption("text " + (i + 1));
                lblText.addStyleName(LABEL_CLASS);
                lblText.setWidth(98, Sizeable.Unit.PERCENTAGE);

                l.addComponent(lblText);
            }

            // apply the panel
            p.setContent(l);
            return p;
        }
        
        Label lblText;
        if (texts.hasTexts() && !hasOnlyWhiteSpace(texts.getFirstText())) {
            lblText = new Label(texts.getFirstText(), ContentMode.TEXT);
            
            if(!Helper.isRTLDisabled() && CommonHelper.containsRTLText(texts.getFirstText()))
            {
              lblText.addStyleName("rtl");
            }
            
        } else {
            lblText = new Label(NO_TEXT);
        }
        
        lblText.setSizeFull();
        lblText.addStyleName(LABEL_CLASS);
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
