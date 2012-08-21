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
package annis.gui;

import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class AboutPanel extends Panel
{
  
  private static final Logger log = LoggerFactory.getLogger(AboutPanel.class);

  private static ClassResource logo_sfb_res;
  private static ClassResource logo_annis_res;
  private VerticalLayout layout;
  
  public AboutPanel(Application app)
  {
    if(logo_sfb_res == null)
    {
      logo_sfb_res = new ClassResource(AboutPanel.class,
      "annis-logo.jpg", app);
    }
    if(logo_annis_res == null)
    {
      logo_annis_res = new ClassResource(AboutPanel.class,
      "annis-logo-128.png", app);
    }
    setSizeFull();
    
    layout = (VerticalLayout) getContent();
    
    layout.setSizeFull();
  }

  @Override
  public void attach()
  {
    super.attach();

    HorizontalLayout hLayout = new HorizontalLayout();
    
    Embedded logoAnnis = new Embedded();
    logoAnnis.setSource(logo_annis_res);
    logoAnnis.setType(Embedded.TYPE_IMAGE);    
    hLayout.addComponent(logoAnnis);
    
    Embedded logoSfb = new Embedded();
    logoSfb.setSource(logo_sfb_res);
    logoSfb.setType(Embedded.TYPE_IMAGE);    
    hLayout.addComponent(logoSfb);
    
    hLayout.setComponentAlignment(logoAnnis, Alignment.MIDDLE_LEFT);
    hLayout.setComponentAlignment(logoSfb, Alignment.MIDDLE_RIGHT);
    
    addComponent(hLayout);
    
    addComponent(new Label("ANNIS is a project of the "
      + "<a href=\"http://www.sfb632.uni-potsdam.de/\">SFB632</a>.", Label.CONTENT_XHTML));
    addComponent(new Label("Homepage: "
      + "<a href=\"http://www.sfb632.uni-potsdam.de/d1/annis/\">"
      + "http://www.sfb632.uni-potsdam.de/d1/annis/</a>.", Label.CONTENT_XHTML));
    addComponent(new Label("Version: " + getApplication().getVersion()));
    
    TextArea txtThirdParty = new TextArea();
    txtThirdParty.setSizeFull();
    
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("The ANNIS team want's to thank these third party software that "
      + "made the ANNIS GUI possible:\n");
    
    File thirdPartyFolder = 
      new File(getApplication().getContext().getBaseDirectory(), "THIRD-PARTY");
    if(thirdPartyFolder.isDirectory())
    {
      for(File c : thirdPartyFolder.listFiles((FileFilter) new WildcardFileFilter("*.txt")))
      {
        if(c.isFile())
        {
          try
          {
            sb.append(FileUtils.readFileToString(c)).append("\n");
          }
          catch (IOException ex)
          {
            log.error("Could not read file", ex);
          }
        }
      }
    }
    
    txtThirdParty.setValue(sb.toString());
    txtThirdParty.setReadOnly(true);
    txtThirdParty.addStyleName("license");
    txtThirdParty.setWordwrap(false);
    
    addComponent(txtThirdParty);
    
    Button btOK = new Button("OK");
    btOK.addListener(new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event)
      {
        Window subwindow = getWindow();
        subwindow.getParent().removeWindow(subwindow);
      }
    });
    addComponent(btOK);
    
    layout.setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(btOK, Alignment.MIDDLE_CENTER);
    layout.setExpandRatio(txtThirdParty, 1.0f);
  }
}
