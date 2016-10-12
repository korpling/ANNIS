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
import annis.libgui.MatchedNodeColors;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.Annotation;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p>
 * <strong>Mappings:</strong><br />
 * <ul>
 * <li>config - path of the visualization configuration file</li>
 * <li>hitmark - if "true" (which is the default) hit are marked in their
 * corresponding colors</li>
 * </ul>
 * </p>
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class HTMLVis extends AbstractVisualizer<Panel>
{

  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);

  private final static Escaper urlPathEscape = UrlEscapers.
    urlPathSegmentEscaper();

  private Map<SNode, Long> mc;

  private String tokenColor = "";

  private boolean hitMark = true;

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
      getGraph(), vi.getDocument());
    String corpusName = corpusPath.get(corpusPath.size() - 1);
    corpusName = urlPathEscape.escape(corpusName);

    String wrapperClassName = "annis-wrapped-htmlvis-"
      + corpusName.replaceAll("[^0-9A-Za-z-]", "_");

    scrollPanel.addStyleName(wrapperClassName);

    String visConfigName = vi.getMappings().getProperty("config");
    String hitMarkConfig = vi.getMappings().getProperty("hitmark", "true");
    hitMark = Boolean.parseBoolean(hitMarkConfig);
    mc = vi.getMarkedAndCovered();

    VisualizationDefinition[] definitions = parseDefinitions(corpusName, vi.
      getMappings());

    if (definitions != null)
    {

      lblResult.setValue(createHTML(vi.getSResult().getDocumentGraph(),
        definitions));

      String labelClass = vi.getMappings().getProperty("class", "htmlvis");
      lblResult.addStyleName(labelClass);

      injectWebFonts(visConfigName, corpusName);
      injectCSS(visConfigName, corpusName, wrapperClassName);
 
      
    }

    if (vi.getMappings().containsKey("debug"))
    {
      TextArea txtDebug = new TextArea();
      txtDebug.setValue(lblResult.getValue());
      txtDebug.setReadOnly(true);
      txtDebug.setWidth("100%");
      Label sep = new Label("<hr/>", ContentMode.HTML);
      VerticalLayout layout = new VerticalLayout(txtDebug, sep, lblResult);
      layout.setSizeUndefined();
      scrollPanel.setContent(layout);
    }
    else
    {
      scrollPanel.setContent(lblResult);
    }

    return scrollPanel;
  }

  @Override
  public List<String> getFilteredNodeAnnotationNames(String toplevelCorpusName,
    String documentName, Properties mappings)
  {
    Set<String> result = null;

    VisualizationDefinition[] definitions = parseDefinitions(toplevelCorpusName,
      mappings);

    if (definitions != null)
    {
      for (VisualizationDefinition def : definitions)
      {
        List<String> sub = def.getMatcher().getRequiredAnnotationNames();
        if (sub == null)
        {
          // a rule requires all annotations, abort
          result = null;
          break;
        }
        else
        {
          if (result == null)
          {
            result = new LinkedHashSet<>();
          }
          result.addAll(sub);
        }
      }
    }

    if (result == null)
    {
      return null;
    }
    else
    {
      return new LinkedList<>(result);
    }
  }

  public VisualizationDefinition[] parseDefinitions(String toplevelCorpusName,
    Properties mappings)
  {
    InputStream inStreamConfigRaw = null;

    String visConfigName = mappings.getProperty("config");

    if (visConfigName == null)
    {
      inStreamConfigRaw = HTMLVis.class.getResourceAsStream("defaultvis.config");
    }
    else
    {
      WebResource resBinary = Helper.getAnnisWebResource().path(
        "query/corpora/").path(toplevelCorpusName).path(toplevelCorpusName)
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

      try (InputStream inStreamConfig = inStreamConfigRaw)
      {

        VisParser p = new VisParser(inStreamConfig);
        return p.getDefinitions();
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
    return null;
  }
  
  private void injectCSS(String visConfigName, String corpusName, String wrapperClassName)
  {
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
      try (InputStream inStreamCSS = inStreamCSSRaw)
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
      catch (IOException ex)
      {
        log.error("Could not parse the HTML visualizer CSS file",
          ex);
        Notification.show(
          "Could not parse the HTML visualizer CSS file", ex.
          getMessage(),
          Notification.Type.ERROR_MESSAGE);
      }
    }
  }
  
  private void injectWebFonts(String visConfigName, String corpusName)
  {
    InputStream inStreamJSONRaw = null;
    if (visConfigName != null)
    {
      WebResource resBinary = Helper.getAnnisWebResource().path(
        "query/corpora/").path(corpusName).path(corpusName)
        .path("binary").path(visConfigName + ".fonts.json");

      ClientResponse response = resBinary.get(ClientResponse.class);
      if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
      {
        inStreamJSONRaw = response.getEntityInputStream();
      }
    }
    if (inStreamJSONRaw != null)
    {
      try (InputStream inStreamJSON = inStreamJSONRaw)
      {
        ObjectMapper mapper = createJsonMapper();
        WebFontList fontConfigList = mapper.readValue(inStreamJSON, WebFontList.class);
       
        for(WebFont fontConfig : fontConfigList.getWebFonts())
        {
        
          if(fontConfig != null && fontConfig.getName() != null) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("@font-face {\n");
            sb.append("  font-family: '" + fontConfig.getName() + "';\n");
            sb.append("  font-weight: '" + fontConfig.getWeight() + "';\n");
            sb.append("  font-style: '" + fontConfig.getStyle() + "';\n");
            
            List<String> sourceDefs = new LinkedList<>();
            for(Map.Entry<String, String> src : fontConfig.getSources().entrySet()) {
              sourceDefs.add("url('" + src.getValue() + "') format('" + src.getKey() + "')");
            }
            
            if(!sourceDefs.isEmpty())
            {
              sb.append("  src: ");
              sb.append(Joiner.on(",\n    ").join(sourceDefs));
              sb.append(";\n");
            }
            
            sb.append("}\n");
            
            UI currentUI = UI.getCurrent();
            if (currentUI instanceof AnnisBaseUI)
            {
              // do not add identical CSS files
              ((AnnisBaseUI) currentUI).injectUniqueCSS(sb.toString());
            }
          }
        }
        
      }
      catch (IOException ex)
      {
        log.error("Could not parse the HTML visualizer web-font configuration file",
          ex);
        Notification.show(
          "Could not parse the HTML visualizer web-font configuration file", ex.
          getMessage(),
          Notification.Type.ERROR_MESSAGE);
      }
    }
  }
  
  private ObjectMapper createJsonMapper()
  {
    ObjectMapper jsonMapper = new ObjectMapper();
    // configure json object mapper
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    jsonMapper.setAnnotationIntrospector(introspector);
    // the json should be human readable
    jsonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,
      true);
    jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
    return jsonMapper;
  }

  public String createHTML(SDocumentGraph graph,
    VisualizationDefinition[] definitions)
  {
    HashMap<VisualizationDefinition, Integer> instruction_priorities = new HashMap<>();

    SortedMap<Long, List<OutputItem>> outputStartTags
      = new TreeMap<>();
    SortedMap<Long, List<OutputItem>> outputEndTags
      = new TreeMap<>();
    StringBuilder sb = new StringBuilder();

    List<SToken> token = graph.getSortedTokenByText();

    //Get metadata for visualizer if stylesheet requires it
    //First check the stylesheet
    Boolean bolMetaTypeFound = false;

    HashMap<String, String> meta = new HashMap<>();
    int def_priority = 0;
    for (VisualizationDefinition vis : definitions)
    {
      if (vis.getOutputter().getType() == SpanHTMLOutputter.Type.META_NAME)
      {
        bolMetaTypeFound = true;
      }
      else //not a meta-annotation, remember order in config file to set priority
      {
        if (vis.getMatcher() instanceof AnnotationNameMatcher)
        {
          instruction_priorities.put(vis, def_priority);
        }
        else if (vis.getMatcher() instanceof AnnotationNameAndValueMatcher)
        {
          instruction_priorities.put(vis, def_priority);
        }
        else if (vis.getMatcher() instanceof TokenMatcher)
        {
          instruction_priorities.put(vis, def_priority);
        }
        def_priority--;
      }
      vis.getOutputter().setMeta(meta);

    }
    if (bolMetaTypeFound == true)        //Metadata is required, get corpus and document name
    {
      //Get corpus and document name
      String strDocName = "";
      String strCorpName = "";
      strDocName = graph.getDocument().getName();
      List<String> corpusPath = CommonHelper.getCorpusPath(graph.getDocument().
        getGraph(), graph.getDocument());
      strCorpName = corpusPath.get(corpusPath.size() - 1);

      //Get metadata and put in hashmap
      List<Annotation> metaData = Helper.getMetaDataDoc(strCorpName, strDocName);
      for (Annotation metaDatum : metaData)
      {
        meta.put(metaDatum.getName(), metaDatum.getValue());
      }
    }

    for (SToken t : token)
    {
      tokenColor = "";
      if (mc.containsKey(t) && hitMark)
      {
        tokenColor = MatchedNodeColors
          .getHTMLColorByMatch(mc.get(t));
      }
      for (VisualizationDefinition vis : definitions)
      {
        String matched = vis.getMatcher().matchedAnnotation(t);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(t, matched, outputStartTags,
            outputEndTags, tokenColor, Objects.firstNonNull(instruction_priorities.get(vis), 0));
        }
      }
    }

    List<SSpan> spans = graph.getSpans();
    for (VisualizationDefinition vis : definitions)
    {

      for (SSpan span : spans)
      {
        tokenColor = "";
        if (mc.containsKey(span) && hitMark)
        {
          tokenColor = MatchedNodeColors
            .getHTMLColorByMatch(mc.get(span));
        }
        String matched = vis.getMatcher().matchedAnnotation(span);
        if (matched != null)
        {
          vis.getOutputter().outputHTML(span, matched, outputStartTags,
            outputEndTags, tokenColor, Objects.firstNonNull(instruction_priorities.get(vis), 0));
        }
      }
    }

    int minStartTagPos = outputStartTags.firstKey().intValue();
    int maxEndTagPos = outputEndTags.lastKey().intValue();

    //Find BEGIN and END instructions if available
    for (VisualizationDefinition vis : definitions)
    {

      if (vis.getMatcher() instanceof PseudoRegionMatcher)
      {
        PseudoRegionMatcher.PseudoRegion psdRegionType = ((PseudoRegionMatcher) vis.
          getMatcher()).getPsdRegion();
        int positionStart = 0;
        int positionEnd = 0;

        if (!outputEndTags.isEmpty() && !outputStartTags.isEmpty() && psdRegionType != null)
        {
          switch (psdRegionType)
          {
            case BEGIN:
              positionStart = positionEnd = Integer.MIN_VALUE;

              // def_priority is now lower than all normal annotation
              instruction_priorities.put(vis, def_priority);
              break;
            case END:
              positionStart = positionEnd = Integer.MAX_VALUE;

              // def_priority is now lower than all normal annotation
              instruction_priorities.put(vis, def_priority);
              break;
            case ALL:
              // use same position as last and first key
              positionStart = minStartTagPos;
              positionEnd = maxEndTagPos;

              // The ALL pseudo-range must enclose everything, thus it get the
              // priority which is one lower than the smallest non BEGIN/END
              // priority.
              instruction_priorities.put(vis, def_priority);
              break;
            default:
              break;
          }
        }

        switch (vis.getOutputter().getType())
        {
          case META_NAME:
            String strMetaVal = meta.
              get(vis.getOutputter().getMetaname().trim());
            if (strMetaVal == null)
            {
              throw new NullPointerException(
                "no such metadata name in document: '" + vis.getOutputter().
                getMetaname().trim() + "'");
            }
            else
            {
              vis.getOutputter().outputAny(positionStart, positionEnd,
                ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(),
                strMetaVal, outputStartTags, outputEndTags,
                Objects.firstNonNull(instruction_priorities.get(vis), 0));
            }
            break;
          case CONSTANT:
            vis.getOutputter().outputAny(positionStart, positionEnd,
              ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(),
              vis.getOutputter().getConstant(), outputStartTags, outputEndTags,
              Objects.firstNonNull(instruction_priorities.get(vis), 0));
            break;
          case EMPTY:
            vis.getOutputter().outputAny(positionStart, positionEnd,
              ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(),
              "", outputStartTags, outputEndTags,
              Objects.firstNonNull(instruction_priorities.get(vis), 0));
            break;
          case ANNO_NAME:
            break; //this shouldn't happen, since the BEGIN/END instruction has no triggering annotation name or value
          case VALUE:
            break; //this shouldn't happen, since the BEGIN/END instruction has no triggering annotation name or value
          case ESCAPED_VALUE:
            break; //this shouldn't happen, since the BEGIN/END instruction has no triggering annotation name or value
          default:
        }

      }

    }

    // get all used indexes
    Set<Long> indexes = new TreeSet<>();
    indexes.addAll(outputStartTags.keySet());
    indexes.addAll(outputEndTags.keySet());

    for (Long i : indexes)
    {
      // output all strings belonging to this token position
      // first the start tags for this position

      // add priorities from instruction_priorities for sorting length ties
      List<OutputItem> unsortedStart = outputStartTags.get(i);
      SortedSet<OutputItem> itemsStart = new TreeSet();
      if (unsortedStart != null)
      {
        Iterator<OutputItem> it = unsortedStart.iterator();
        while (it.hasNext())
        {
          OutputItem s = it.next();
          itemsStart.add(s);
        }
      }

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
      List<OutputItem> unsortedEnd = outputEndTags.get(i);
      SortedSet<OutputItem> itemsEnd = new TreeSet();
      if (unsortedEnd != null)
      {
        Iterator<OutputItem> it = unsortedEnd.iterator();
        while (it.hasNext())
        {
          OutputItem s = it.next();
          itemsEnd.add(s);
        }
      }

      {
        List<OutputItem> itemsEndReverse = new LinkedList<>(itemsEnd);
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
