/*
 * Copyright 2014 SFB 632.
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

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.vaadin.server.widgetsetutils.ConnectorBundleLoaderFactory;
import com.vaadin.shared.ui.Connect.LoadStyle;
import java.util.HashSet;
import java.util.Set;

public class OptimizedConnectorBundleLoaderFactory extends
  ConnectorBundleLoaderFactory
{

  private Set<String> eagerConnectors = new HashSet<>();

  
  {
    eagerConnectors.add(com.vaadin.client.ui.ui.UIConnector.class.getName());
    eagerConnectors.add(
      com.vaadin.client.ui.gridlayout.GridLayoutConnector.class.getName());
    eagerConnectors.add(com.vaadin.client.ui.tabsheet.TabsheetConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.ui.accordion.AccordionConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.ui.table.TableConnector.class.
      getName());
    eagerConnectors.add(
      com.vaadin.client.ui.orderedlayout.VerticalLayoutConnector.class.getName());
    eagerConnectors.add(com.vaadin.client.ui.textarea.TextAreaConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.ui.button.ButtonConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.ui.label.LabelConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.ui.textfield.TextFieldConnector.class.
      getName());
    eagerConnectors.add(
      com.vaadin.client.ui.listselect.ListSelectConnector.class.getName());
    eagerConnectors.add(com.vaadin.client.ui.combobox.ComboBoxConnector.class.
      getName());
    eagerConnectors.add(
      com.vaadin.client.ui.orderedlayout.HorizontalLayoutConnector.class.
      getName());
    eagerConnectors.add(com.vaadin.client.JavaScriptExtension.class.getName());
    eagerConnectors.add(
      com.vaadin.client.extensions.javascriptmanager.JavaScriptManagerConnector.class.
      getName());
    eagerConnectors.add(
      org.vaadin.hene.popupbutton.widgetset.client.ui.PopupButtonConnector.class.
      getName());
  }

  @Override
  protected LoadStyle getLoadStyle(JClassType connectorType)
  {
    if (eagerConnectors.contains(connectorType.getQualifiedBinaryName()))
    {
      return LoadStyle.EAGER;
    }
    else
    {
      // Loads all other connectors immediately after the initial view has
      // been rendered
      return LoadStyle.DEFERRED;
    }
  }
}
