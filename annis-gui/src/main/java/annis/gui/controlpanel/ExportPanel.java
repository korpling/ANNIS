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
package annis.gui.controlpanel;

import annis.gui.Helper;
import annis.gui.exporter.Exporter;
import annis.gui.exporter.GridExporter;
import annis.gui.exporter.TextExporter;
import annis.gui.exporter.WekaExporter;
import annis.security.AnnisUser;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Window.Notification;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ExportPanel extends Panel implements Button.ClickListener
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExportPanel.class);

  private static final Exporter[] EXPORTER = new Exporter[]
  {
    new WekaExporter(),
    new TextExporter(),
    new GridExporter()
  };
  private ComboBox cbExporter;
  private ComboBox cbLeftContext;
  private ComboBox cbRightContext;
  private TextField txtParameters;
  private Button btExport;
  private Map<String, Exporter> exporterMap;
  private QueryPanel queryPanel;
  private CorpusListPanel corpusListPanel;
  
  private final static Random rand = new Random();

  public ExportPanel(QueryPanel queryPanel, CorpusListPanel corpusListPanel)
  {
    this.queryPanel = queryPanel;
    this.corpusListPanel = corpusListPanel;

    setSizeFull();

    FormLayout layout = new FormLayout();
    layout.setSizeFull();
    setContent(layout);

    cbExporter = new ComboBox("Exporter");
    cbExporter.setNewItemsAllowed(false);
    cbExporter.setNullSelectionAllowed(false);
    exporterMap = new HashMap<String, Exporter>();
    for(Exporter e : EXPORTER)
    {
      String name = e.getClass().getSimpleName();
      exporterMap.put(name, e);
      cbExporter.addItem(name);
    }
    cbExporter.setValue(EXPORTER[0].getClass().getSimpleName());

    layout.addComponent(cbExporter);

    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);

    cbLeftContext.addValidator(new IntegerValidator("must be a number"));
    cbRightContext.addValidator(new IntegerValidator("must be a number"));

    for(String s : SearchOptionsPanel.PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(s);
      cbRightContext.addItem(s);
    }


    cbLeftContext.setValue("5");
    cbRightContext.setValue("5");

    layout.addComponent(cbLeftContext);
    layout.addComponent(cbRightContext);

    txtParameters = new TextField("Parameters");
    layout.addComponent(txtParameters);

    btExport = new Button("Perform Export");
    btExport.addListener((Button.ClickListener) this);
    layout.addComponent(btExport);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    try
    {
      String exporterName = (String) cbExporter.getValue();
      final Exporter exporter = exporterMap.get(exporterName);
      if(exporter != null)
      {
        if(corpusListPanel.getSelectedCorpora().isEmpty())
        {
          getWindow().showNotification("Please select a corpus",
            Notification.TYPE_WARNING_MESSAGE);
          return;
        }

        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(out);

        new Thread(new Runnable()
        {

          @Override
          public void run()
          {
            try
            {
              exporter.convertText(queryPanel.getQuery(),
                Integer.parseInt((String) cbLeftContext.getValue()),
                Integer.parseInt((String) cbRightContext.getValue()),
                corpusListPanel.getSelectedCorpora(),
                null, (String) txtParameters.getValue(),
                Helper.getAnnisWebResource(getApplication()).path("query"),
                new OutputStreamWriter(out, "UTF-8"));
            }
            catch (UnsupportedEncodingException ex)
            {
              log.error(null, ex);
            }
          }
        }).start();

        StreamResource resource = new StreamResource(new StreamResource.StreamSource()
        {

          @Override
          public InputStream getStream()
          {
            return in;
          }
        }, exporterName + "_" + rand.nextInt(Integer.MAX_VALUE), getApplication());

        getWindow().open(
          new ExternalResource(getApplication().getRelativeLocation(resource),"application/x-unknown"));

      }
    }
    catch(IOException ex)
    {
      log.error(null, ex);
    }
  }
}
