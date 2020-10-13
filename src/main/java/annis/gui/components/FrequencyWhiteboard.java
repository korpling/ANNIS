/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.components;

import static annis.libgui.Helper.encodeGeneric;

import annis.gui.frequency.FrequencyResultPanel;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.ui.AbstractJavaScriptComponent;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.api.model.FrequencyTableRow;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@JavaScript(value = { "flotr2.js", "vaadin://jquery.js", "frequencychart.js" })
public class FrequencyWhiteboard extends AbstractJavaScriptComponent implements OnLoadCallbackExtension.Callback {
    /**
     * 
     */
    private static final long serialVersionUID = -3666118907756684861L;

    public enum Scale {
        LINEAR("linear"), LOG10("logarithmic");

        public final String desc;

        Scale(String desc) {
            this.desc = desc;
        }
    }

    public static final float PIXEL_PER_VALUE = 45.0f;

    public static final float ADDTIONAL_PIXEL_WIDTH = 100.0f;

    private List<String> labels;
    private List<Integer> values;
    private Scale lastScale;
    private String lastFont;
    private float lastFontSize = 10.0f;

    public FrequencyWhiteboard(final FrequencyResultPanel freqPanel) {
        setHeight("100%");
        setWidth("200px");
        addStyleName("frequency-chart");

        addFunction("selectRow", arguments -> freqPanel.selectRow((int) arguments.getNumber(0)));

        OnLoadCallbackExtension ext = new OnLoadCallbackExtension(this);
        ext.extend(this);

    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        if (labels != null && values != null && lastScale != null && lastFont != null) {
            callFunction("showData", encodeGeneric(labels), encodeGeneric(values), lastScale.desc, lastFont,
                    lastFontSize);
        }
    }

    @Override
    public boolean onCompononentLoaded(AbstractClientConnector source) {
        if (labels != null && values != null && lastScale != null && lastFont != null) {
            callFunction("showData", encodeGeneric(labels), encodeGeneric(values), lastScale.desc, lastFont,
                    lastFontSize);
        }
        return true;
    }

    public void setFrequencyData(List<FrequencyTableRow> table, Scale scale, String font,
        float fontSize) {
        labels = new LinkedList<>();
        values = new LinkedList<>();

        for (FrequencyTableRow e : table) {
          labels.add(StringUtils.join(e.getValues(), "/") + " (" + e.getCount() + ")");
            values.add(e.getCount());
        }
        setWidth(ADDTIONAL_PIXEL_WIDTH + (PIXEL_PER_VALUE * (float) values.size()), Unit.PIXELS);
        lastScale = scale;
        lastFont = font;
        lastFontSize = fontSize;

        // callFunction("showData", labels, values, lastScale.desc, lastFont,
        // lastFontSize);
    }

}
