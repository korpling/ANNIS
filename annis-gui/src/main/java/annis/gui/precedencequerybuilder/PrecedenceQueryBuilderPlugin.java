/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.precedencequerybuilder;

import annis.gui.Helper;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.querybuilder.QueryBuilderPlugin;
import annis.gui.precedencequerybuilder.VerticalNode;
import annis.gui.precedencequerybuilder.AddMenu;
import annis.model.Annotation;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;
//the following added by Martin:
import com.vaadin.ui.Component;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import java.util.Iterator;
import java.util.ArrayList;
import net.xeoh.plugins.base.annotations.PluginImplementation;



/**
 *
 * @author tom
 */

@PluginImplementation
public class PrecedenceQueryBuilderPlugin implements QueryBuilderPlugin<PrecedenceQueryBuilder>
{

  @Override
  public String getShortName()
  {
    return "precedencequerybuilder";
  }

  @Override
  public String getCaption()
  {
    return "Precedence (Word sequences)";
  }

  @Override
  public PrecedenceQueryBuilder createComponent(ControlPanel controlPanel)
  {
    return new PrecedenceQueryBuilder(controlPanel);
  }
}
