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

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
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
    
  private VerticalLayout layout;
  
  public AboutPanel()
  {
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
    logoAnnis.setSource(new ThemeResource("annis-logo-128.png"));
    logoAnnis.setType(Embedded.TYPE_IMAGE);    
    hLayout.addComponent(logoAnnis);
    
    Embedded logoSfb = new Embedded();
    logoSfb.setSource(new ThemeResource("sfb-logo.jpg"));
    logoSfb.setType(Embedded.TYPE_IMAGE);    
    hLayout.addComponent(logoSfb);
    
    Link lnkFork = new Link();
    lnkFork.setResource(new ExternalResource("https://github.com/korpling/ANNIS"));
    lnkFork.setIcon(new ExternalResource("https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"));
    lnkFork.setTargetName("_blank");
    hLayout.addComponent(lnkFork);
    
    hLayout.setComponentAlignment(logoAnnis, Alignment.MIDDLE_LEFT);
    hLayout.setComponentAlignment(logoSfb, Alignment.MIDDLE_RIGHT);
    hLayout.setComponentAlignment(lnkFork, Alignment.TOP_RIGHT);
    
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
