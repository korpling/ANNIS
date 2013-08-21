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
package annis.gui.controlpanel;

import annis.gui.components.HelpButton;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ExportOptionsPanel extends FormLayout
{
  private ComboBox cbLeftContext;
  private ComboBox cbRightContext;
  private TextField txtAnnotationKeys;
  private TextField txtParameters;
  
  public ExportOptionsPanel()
  {
    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);

    cbLeftContext.addValidator(new IntegerRangeValidator("must be a number",
      Integer.MIN_VALUE, Integer.MAX_VALUE));
    cbRightContext.addValidator(new IntegerRangeValidator("must be a number",
      Integer.MIN_VALUE, Integer.MAX_VALUE));

    for (Integer i : SearchOptionsPanel.PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(i);
      cbRightContext.addItem(i);
    }


    cbLeftContext.setValue(5);
    cbRightContext.setValue(5);

    addComponent(cbLeftContext);
    addComponent(cbRightContext);

    txtAnnotationKeys = new TextField("Annotation Keys");
    txtAnnotationKeys.setDescription("Some exporters will use this comma "
      + "seperated list of annotation keys to limit the exported data to these "
      + "annotations.");
    addComponent(new HelpButton(txtAnnotationKeys));

    txtParameters = new TextField("Parameters");
    txtParameters.setDescription("You can input special parameters "
      + "for certain exporters. See the description of each exporter "
      + "(‘?’ button above) for specific parameter settings.");
    addComponent(new HelpButton(txtParameters));
  }
}
