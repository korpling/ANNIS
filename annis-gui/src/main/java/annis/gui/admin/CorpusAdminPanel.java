/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

package annis.gui.admin;

import annis.gui.admin.view.CorpusListView;
import annis.gui.admin.view.UIView.Listener;
import annis.gui.admin.view.UserListView;
import annis.security.User;
import annis.service.objects.AnnisCorpus;
import com.vaadin.ui.Panel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusAdminPanel extends Panel
  implements CorpusListView
{
  
  private final List<CorpusListView.Listener> listeners = new LinkedList<>();

  public CorpusAdminPanel()
  {
  }
  
  @Override
  public void addListener(CorpusListView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void setAvailableCorpora(Collection<AnnisCorpus> corpora)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
