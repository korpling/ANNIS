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
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p>
 * <strong>Mappings:</strong><br />
 * <ul>
 * <li>visconfigpath - path of the visualization configuration file</li>
 * </ul>
 * </p>
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
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
    return false;
  }

  @Override
  public Panel createComponent(VisualizerInput vi, VisualizationToggle vt)
  {
    Panel scrollPanel = new Panel();
    scrollPanel.setSizeFull();
    Label lblResult = new Label("ERROR", ContentMode.HTML);
    lblResult.setSizeUndefined();

    List<String> corpusPath = CommonHelper.getCorpusPath(vi.getDocument().
      getSCorpusGraph(), vi.getDocument());
    String corpusName = corpusPath.get(corpusPath.size() - 1);
    try
    {
      corpusName = URLEncoder.encode(corpusName, "UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error("UTF-8 was not known as encoding", ex);
    }

    String wrapperClassName = "annis-wrapped-htmlvis-"
      + corpusName.replaceAll("[^0-9A-Za-z-]", "_");

    scrollPanel.addStyleName(wrapperClassName);

    InputStream inStreamConfigRaw = null;

    String visConfigName = vi.getMappings().getProperty("config");

    if (visConfigName == null)
    {
      inStreamConfigRaw = HTMLVis.class.getResourceAsStream("defaultvis.config");
    }
    else
    {
      WebResource resBinary = Helper.getAnnisWebResource().path(
        "query/corpora/").path(corpusName).path(corpusName)
        .path("binary").path(visConfigName + ".config");

      ClientResponse response = resBinary.get(ClientResponse.class);
      if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
      {
        inStreamConfigRaw = response.getEntityInputStream();
      }
    }

    if (inStreamConfigRaw == null)
    {
      Notification.show("ERROR: html visualization configuration \""
        + visConfigName
        + "\" not found in database", Notification.Type.ERROR_MESSAGE);
    }
    else
    {

      try(InputStream inStreamConfig = inStreamConfigRaw)
      {

        VisParser p = new VisParser(inStreamConfig);
        VisualizationDefinition[] definitions = p.getDefinitions();

        lblResult.setValue(createHTML(vi.getSResult().getSDocumentGraph(),
          definitions));

        String labelClass = vi.getMappings().getProperty("class", "htmlvis");
        lblResult.addStyleName(labelClass);

        InputStream inStreamCSSRaw = null;
        if (visConfigName == null)
        {
          inStreamCSSRaw = HTMLVis.class.getResourceAsStream("htmlvis.css");
        }
        else
        {
          WebResource resBinary = Helper.getAnnisWebResource().path(
            "query/corpora/").path(corpusName).path(corpusName)
            .path("binary").path(visConfigName + ".css");

          ClientResponse response = resBinary.get(ClientResponse.class);
          if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
          {
            inStreamCSSRaw = response.getEntityInputStream();
          }
        }
        if (inStreamCSSRaw != null)
        {
          try(InputStream inStreamCSS = inStreamCSSRaw)
          {
            String cssContent = IOUtils.toString(inStreamCSS);
            UI currentUI = UI.getCurrent();
            if (currentUI instanceof AnnisBaseUI)
            {
              // do not add identical CSS files
              ((AnnisBaseUI) currentUI).injectUniqueCSS(cssContent,
                wrapperClassName);
            }
          }
        }

      }
      catch (IOException | VisParserException ex)
      {
        log.error("Could not parse the HTML visualization configuration file",
          ex);
        Notification.show(
          "Could not parse the HTML visualization configuration file", ex.
          getMessage(),
          Notification.Type.ERROR_MESSAGE);
      }
    }

    if (vi.getMappings().containsKey("debug"))
    {
      Label lblDebug = new Label(lblResult.getValue(), ContentMode.PREFORMATTED);
      Label sep = new Label("<hr/>", ContentMode.HTML);
      VerticalLayout layout = new VerticalLayout(lblDebug, sep, lblResult);
      scrollPanel.setContent(layout);
    }
    else
    {
      scrollPanel.setContent(lblResult);
    }

    return scrollPanel;
  }

  private String createHTML(SDocumentGraph graph,
    VisualizationDefinition[] definitions)
  {
    SortedMap<Long, SortedSet<OutputItem>> outputStartTags
      = new TreeMap<Long, SortedSet<OutputItem>>();
    SortedMap<Long, SortedSet<OutputItem>> outputEndTags
      = new TreeMap<Long, SortedSet<OutputItem>>();
    StringBuilder sb = new StringBuilder();

    EList<SToken> token = graph.getSortedSTokenByText();

    for (SToken t : token)
    {

      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(t);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(t, matched, outputStartTags,
            outputEndTags);
        }
      }
    }

    List<SSpan> spans = graph.getSSpans();
    for (VisualizationDefinition vis : definitions)
    {
      for (SSpan span : spans)
      {
        String matched = vis.getMatcher().matchedAnnotation(span);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(span, matched, outputStartTags,
            outputEndTags);
        }
      }
    }

    // get all used indexes
    Set<Long> indexes = new TreeSet<Long>();
    indexes.addAll(outputStartTags.keySet());
    indexes.addAll(outputEndTags.keySet());

    for (Long i : indexes)
    {
      // output all strings belonging to this token position

      // first the start tags for this position
      SortedSet<OutputItem> itemsStart = outputStartTags.get(i);
      if (itemsStart != null)
      {
        Iterator<OutputItem> it = itemsStart.iterator();
        boolean first = true;
        while (it.hasNext())
        {
          OutputItem s = it.next();
          if (!first)
          {
            sb.append("-->");
          }
          first = false;
          sb.append(s.getOutputString());
          if (it.hasNext())
          {
            sb.append("<!--\n");
          }
        }
      }
      // then the end tags for this position, but inverse their order
      SortedSet<OutputItem> itemsEnd = outputEndTags.get(i);
      if (itemsEnd != null)
      {
        List<OutputItem> itemsEndReverse = new LinkedList<OutputItem>(itemsEnd);
        Collections.reverse(itemsEndReverse);
        for (OutputItem s : itemsEndReverse)
        {
          sb.append(s.getOutputString());
        }
      }

    }

    return sb.toString();
  }

}
