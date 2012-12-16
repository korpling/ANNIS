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
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.HelpFieldWrapper;

/**
 *
 * @author thomas
 */
public class ExportPanel extends FormLayout implements Button.ClickListener
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExportPanel.class);

  private static final Exporter[] EXPORTER = new Exporter[]
  {
    new WekaExporter(),
    new TextExporter(),
    new GridExporter()
  };
  
  private final Map<String,String> help4Exporter = new HashMap<String,String>();
  
  private ContextHelp help;
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

    setWidth("99%");
    setHeight("99%");
    addStyleName("contextsensible-formlayout");
    
    initHelpMessages();
    
    help = new ContextHelp();
    addComponent(help);

    cbExporter = new ComboBox("Exporter");
    cbExporter.setNewItemsAllowed(false);
    cbExporter.setNullSelectionAllowed(false);
    cbExporter.setImmediate(true);
    exporterMap = new HashMap<String, Exporter>();
    for(Exporter e : EXPORTER)
    {
      String name = e.getClass().getSimpleName();
      exporterMap.put(name, e);
      cbExporter.addItem(name);
    }
    cbExporter.setValue(EXPORTER[0].getClass().getSimpleName());
    cbExporter.addListener(new ExporterSelectionHelpListener());
    help.addHelpForComponent(cbExporter, help4Exporter.get((String) cbExporter.getValue()));
    
    addComponent(new HelpFieldWrapper(cbExporter, help));

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

    addComponent(cbLeftContext);
    addComponent(cbRightContext);

    txtParameters = new TextField("Parameters");
    help.addHelpForComponent(txtParameters, "You can input special parameters "
      + "for certain exporters. See the description of each exporter "
      + "(‘?’ button above) for specific parameter settings.");
    addComponent(new HelpFieldWrapper(txtParameters, help));
    

    btExport = new Button("Perform Export");
    btExport.addListener((Button.ClickListener) this);
    addComponent(btExport);
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
  
  private void initHelpMessages()
  {
    help4Exporter.put(EXPORTER[0].getClass().getSimpleName(),
      "The WEKA Exporter exports only the "
      + "values of the elements searched for by the user, ignoring the context "
      + "around search results. The values for all annotations of each of the "
      + "found nodes is given in a comma-separated table (CSV). At the top of "
      + "the export, the names of the columns are given in order according to "
      + "the WEKA format.<br/><br/>"
      + "Parameters: <br/>"
      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
      + "<code>metakeys=title,documentname</code>)");

    help4Exporter.put(EXPORTER[1].getClass().getSimpleName(),
      "The Text Exporter exports just the plain text of every search result and "
      + "its context, one line per result.");

    help4Exporter.put(EXPORTER[2].getClass().getSimpleName(),
      "The Grid Exporter can export all annotations of a search result and its "
      + "context. Each annotation layer is represented in a separate line, and the "
      + "tokens covered by each annotation are given as number ranges after each "
      + "annotation in brackets. To suppress token numbers, input numbers=false "
      + "into the parameters box below. To display only a subset of annotations "
      + "in any order, input e.g. keys=tok,pos,cat to show tokens and the "
      + "annotations pos and cat. Combine both parameters like this:<br />"
      + "keys=tok,pos;numbers=false.");
  }
  
  public class ExporterSelectionHelpListener implements Property.ValueChangeListener
  {
    
    @Override
    public void valueChange(ValueChangeEvent event)
    {
      String helpMessage = help4Exporter.get((String) event.getProperty().getValue());
      if(helpMessage != null)
      {
        help.addHelpForComponent(cbExporter, helpMessage);
      }
      else
      {
        help.addHelpForComponent(cbExporter, "No help available for this exporter");
      }
    }
    
  }
  
}
