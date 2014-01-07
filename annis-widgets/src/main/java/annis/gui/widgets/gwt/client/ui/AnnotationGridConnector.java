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
package annis.gui.widgets.gwt.client.ui;

import annis.gui.widgets.grid.AnnotationGrid;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Connect(AnnotationGrid.class)
public class AnnotationGridConnector extends LegacyConnector
{

  @Override
  public VAnnotationGrid getWidget()
  {
    return (VAnnotationGrid) super.getWidget();
  }
  
}
