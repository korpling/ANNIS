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
import annis.service.objects.AnnisAttribute;
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.HelpFieldWrapper;

/**
 *
 * @author thomas
 */
public class SearchOptionsPanel extends FormLayout
{  
  public static final String KEY_DEFAULT_SEGMENTATION = "default-segmentation";
  public static final String NULL_SEGMENTATION_VALUE = "tokens";

  private static final Logger log = LoggerFactory.getLogger(SearchOptionsPanel.class);
  
  private ComboBox cbLeftContext;
  private ComboBox cbRightContext;
  private ComboBox cbResultsPerPage;
  private ComboBox cbSegmentation;
  // TODO: make this configurable
  private static final String[] PREDEFINED_PAGE_SIZES = new String[]
  {
    "1", "2", "5", "10", "15", "20", "25"
  };
  static final String[] PREDEFINED_CONTEXTS = new String[]
  {
    "0", "1", "2", "5", "10"
  };

  public SearchOptionsPanel()
  {
    setWidth("99%");
    setHeight("99%");
    
    addStyleName("contextsensible-formlayout");
    
    ContextHelp help = new ContextHelp();
    addComponent(help);
    
    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");
    cbResultsPerPage = new ComboBox("Results Per Page");

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);
    cbResultsPerPage.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);
    cbResultsPerPage.setNewItemsAllowed(true);

    cbLeftContext.addValidator(new IntegerValidator("must be a number"));
    cbRightContext.addValidator(new IntegerValidator("must be a number"));
    cbResultsPerPage.addValidator(new IntegerValidator("must be a number"));

    for (String s : PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(s);
      cbRightContext.addItem(s);
    }

    for (String s : PREDEFINED_PAGE_SIZES)
    {
      cbResultsPerPage.addItem(s);
    }

    cbSegmentation = new ComboBox("Show context in");
    cbSegmentation.setTextInputAllowed(false);
    cbSegmentation.setNullSelectionAllowed(true);
    cbSegmentation.setNullSelectionItemId(NULL_SEGMENTATION_VALUE);
    cbSegmentation.addItem(NULL_SEGMENTATION_VALUE);
    cbSegmentation.setValue(NULL_SEGMENTATION_VALUE);
    
    cbLeftContext.setValue("5");
    cbRightContext.setValue("5");
    cbResultsPerPage.setValue("10");

    addComponent(cbLeftContext);
    addComponent(cbRightContext);
    addComponent(new HelpFieldWrapper(cbSegmentation, help));
    addComponent(cbResultsPerPage);
    
    help.addHelpForComponent(cbSegmentation, "If corpora with multiple "
      + "context definitions are selected, a list of available context units will be "
      + "displayed. By default context is calculated in ‘tokens’ "
      + "(e.g. 5 minimal units to the left and right of a search result). "
      + "Some corpora might offer further context definitions, e.g. in "
      + "syllables, word forms belonging to different speakers, normalized or "
      + "diplomatic segmentations of a manuscript, etc.");
  }

  public void updateSegmentationList(Set<String> corpora)
  {
    // get all segmentation paths
    WebResource service = Helper.getAnnisWebResource(getApplication());
    if (service != null)
    {

      List<AnnisAttribute> attributes = new LinkedList<AnnisAttribute>();
      
      String lastSelection = (String) cbSegmentation.getValue();
      cbSegmentation.removeAllItems();
      
      cbSegmentation.addItem(NULL_SEGMENTATION_VALUE);
      
      for (String corpus : corpora)
      {
        try
        {
          attributes.addAll(
            service.path("query").path("corpora").path(URLEncoder.encode(corpus, "UTF-8"))
            .path("annotations").queryParam(
            "fetchvalues", "true").queryParam("onlymostfrequentvalues", "true").
            get(new GenericType<List<AnnisAttribute>>()
          {
          }));
        }
        catch (UnsupportedEncodingException ex)
        {
          log.error(null, ex);
        }
        
        CorpusConfig config = Helper.getCorpusConfig(corpus, getApplication(), getWindow());
        
        if(config.getConfig().containsKey(KEY_DEFAULT_SEGMENTATION))
        {
          lastSelection = config.getConfig().get(KEY_DEFAULT_SEGMENTATION);
        }
      }


      for (AnnisAttribute att : attributes)
      {
        if (AnnisAttribute.Type.segmentation == att.getType()
          && att.getName() != null)
        {
          cbSegmentation.addItem(att.getName());
        }
      }
      
      cbSegmentation.setValue(lastSelection);
      
    }
  }

  public void setLeftContext(int context)
  {
    cbLeftContext.setValue("" + context);
  }

  public int getLeftContext()
  {
    int result = 5;
    try
    {
      result = Integer.parseInt((String) cbLeftContext.getValue());
    }
    catch (Exception ex)
    {
    }

    return Math.max(0, result);
  }

  public int getRightContext()
  {
    int result = 5;
    try
    {
      result = Integer.parseInt((String) cbRightContext.getValue());
    }
    catch (Exception ex)
    {
    }

    return Math.max(0, result);
  }

  public void setRightContext(int context)
  {
    cbRightContext.setValue("" + context);
  }

  public int getResultsPerPage()
  {
    int result = 10;
    try
    {
      result = Integer.parseInt((String) cbResultsPerPage.getValue());
    }
    catch (Exception ex)
    {
    }

    return Math.max(0, result);
  }
  
  public String getSegmentationLayer()
  {
    return (String) cbSegmentation.getValue();
  }
}
