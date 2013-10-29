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
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Renders the plain text from the text table. Therefore this visualizer is
 * faster, than a visualizer which has to traverse a {@link SaltProject} for
 * getting the spanned {@link STextualDS}.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class RawTextVisualizer extends AbstractVisualizer<Panel> {

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

        RawTextWrapper texts = visInput.getRawText();
        Panel p = new Panel();

        if (texts.hasMultipleTexts()) {
            for (int i = 0; i < texts.getTexts().size(); i++) {
                Label text = new Label(texts.getTexts().get(i), ContentMode.TEXT);
                text.setCaption("text " + (i + 1));
                p.setContent(text);
            }
        } else if (visInput.getRawText().hasTexts()) {
            Label text = new Label(texts.getFirstText(), ContentMode.TEXT);
            p.setContent(text);
        } else {
            Label text = new Label("n/a");
            p.setContent(text);
        }

        return p;
    }

}
