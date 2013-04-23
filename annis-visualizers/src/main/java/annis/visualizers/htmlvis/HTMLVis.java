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
package annis.visualizers.htmlvis;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_TOKENINDEX;
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisBinaryMetaData;
import annis.visualizers.component.grid.EventExtractor;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.TreeMap;
import javax.print.attribute.standard.DocumentName;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.cssinject.CSSInject;

/**
 *
 * <p>
 * <strong>Mappings:</strong><br />
 * <ul>
 * <li>visconfigpath - path of the visualization configuration file</li>
 * </ul>
 * </p>
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class HTMLVis extends AbstractVisualizer<Panel>
{

  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);
  

  @Override
  public String getShortName()
  {
    return "html";
  }

  @Override
  public boolean isUsingText()
  {
    return true;
  }
  
  

  @Override
  public Panel createComponent(VisualizerInput vi, VisualizationToggle vt)
  {
    Panel scrollPanel = new Panel();
    scrollPanel.setSizeFull();
    Label lblResult = new Label("ERROR", ContentMode.HTML);
    lblResult.setSizeUndefined();
    
    List<String> corpusPath =
      CommonHelper.getCorpusPath(vi.getDocument().getSCorpusGraph(), vi.getDocument());
    String corpusName = corpusPath.get(corpusPath.size() - 1);
    String documentName = corpusPath.get(0);
    try
    {
      corpusName = URLEncoder.encode(corpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error("UTF-8 was not known as encoding, expect non-working audio", ex);
    }
    
    
    WebResource resMeta = Helper.getAnnisWebResource().path(
      "query/corpora/").path(corpusName).path(documentName)
      .path("binary/meta");
     List<AnnisBinaryMetaData> binaryMeta = 
       resMeta.get(new GenericType<List<AnnisBinaryMetaData>>() {});
    
    try
    {
      // TODO: can we load the file from the corpus media files? Or how do we bundle these kind of files with a corpus?
      String visConfigName = vi.getMappings().getProperty("visconfig");
      InputStream inStreamConfig = null;
      if(visConfigName == null)
      {
        inStreamConfig = HTMLVis.class.getResourceAsStream("defaultvis.config");
      }
      else
      {
        String title = visConfigName + ".config";
        for(AnnisBinaryMetaData m : binaryMeta)
        {
          if(title.equals(m.getFileName()))
          {            
            WebResource resBinary = Helper.getAnnisWebResource().path(
              "query/corpora/").path(corpusName).path(documentName)
              .path("binary").path("0").path("" + m.getLength())
              .queryParam("title", m.getFileName());
            AnnisBinary binary = resBinary.get(AnnisBinary.class);
            
            inStreamConfig = new ByteArrayInputStream(binary.getBytes());
            break;
          }
        }
      }
      
      if(inStreamConfig == null)
      {
        Notification.show("ERROR: html visualization configuration \"" 
          + visConfigName 
          +  "\" not found in database",Notification.Type.ERROR_MESSAGE);
      }
      else
      {
      
        VisParser p = new VisParser(inStreamConfig);
        VisualizationDefinition[] definitions = p.getDefinitions();

        List<String> annos = EventExtractor.computeDisplayAnnotations(vi);

        lblResult.setValue(createHTML(vi.getSResult().getSDocumentGraph(), annos,
          definitions));

        // TODO: do not add CSSInject multiple times
        InputStream inStreamCSS = null;
        if(visConfigName == null)
        {
           inStreamCSS = HTMLVis.class.getResourceAsStream("htmlvis.css");
        }
        else
        {
          String title = visConfigName + ".css";
          for (AnnisBinaryMetaData m : binaryMeta)
          {
            if (title.equals(m.getFileName()))
            {
              WebResource resBinary = Helper.getAnnisWebResource().path(
                "query/corpora/").path(corpusName).path(documentName)
                .path("binary").path("0").path("" + m.getLength())
                .queryParam("title", m.getFileName());
              AnnisBinary binary = resBinary.get(AnnisBinary.class);

              inStreamCSS = new ByteArrayInputStream(binary.getBytes());
              break;
            }
          }
        }
        if(inStreamCSS != null)
        {
          String cssContent = IOUtils.toString(inStreamCSS);
          CSSInject cssInject = new CSSInject(UI.getCurrent());
          cssInject.setStyles(cssContent);
        }
      }
    }
    catch (IOException ex)
    {
      log.error("Could not parse the HTML visualization configuration file", ex);
    }

    scrollPanel.setContent(lblResult);
    
    return scrollPanel;
  }

  private String createHTML(SDocumentGraph graph, List<String> annos,
    VisualizationDefinition[] definitions)
  {
    TreeMap<Long, List<String>> output = new TreeMap<Long, List<String>>();
    StringBuilder sb = new StringBuilder();

    EList<SToken> token = graph.getSortedSTokenByText();

    for (SSpan span : graph.getSSpans())
    {
      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(span);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(span, matched, output);
        }
      }
    }
    for (SToken t : token)
    {
      // get token index
      long currentIndex = t.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).
        getSValueSNUMERIC();
      
      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(t);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(t, matched, output);
        }
      }
      
      // output all strings belonging to this token position
      List<String> values = output.get(currentIndex);
      if(values != null)
      {
        for(String s : values)
        {
          sb.append(s);
        }
      }
    }

    return sb.toString();
  }
  
}
