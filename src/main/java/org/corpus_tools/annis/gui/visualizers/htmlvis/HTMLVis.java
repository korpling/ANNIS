/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.htmlvis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Joiner;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.gui.AnnisBaseUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.MatchedNodeColors;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * <p>
 * <strong>Mappings:</strong><br />
 * <ul>
 * <li>config - path of the visualization configuration file</li>
 * <li>hitmark - if "true" (which is the default) hit are marked in their corresponding colors</li>
 * </ul>
 * </p>
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class HTMLVis extends AbstractVisualizer {

  /**
   * 
   */
  private static final long serialVersionUID = 4917299874730026082L;

  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);


  private Map<SNode, Long> mc;

  private String tokenColor = "";

  private boolean hitMark = true;

  @Override
  public Panel createComponent(VisualizerInput vi, VisualizationToggle vt) {
    Panel scrollPanel = new Panel();
    scrollPanel.setSizeFull();
    Label lblResult = new Label("ERROR", ContentMode.HTML);
    lblResult.setSizeUndefined();

    List<String> corpusPath = Helper.getCorpusPath(vi.getDocument().getGraph(), vi.getDocument());
    String corpusName = corpusPath.get(corpusPath.size() - 1);
    log.debug("Creating htmlvis component for path {}", corpusPath.toArray());

    // Get the (internally escaped) node name of the root corpus, fallback to unescaped corpus name
    List<SNode> rootCorpora = vi.getDocument().getGraph().getRoots();
    String rootCorpusId =
        rootCorpora != null && rootCorpora.size() == 1 ? rootCorpora.get(0).getId() : corpusName;
    rootCorpusId = Helper.removeSaltPrefix(rootCorpusId);

    String wrapperClassName =
        "annis-wrapped-htmlvis-" + corpusName.replaceAll("[^0-9A-Za-z-]", "_");

    scrollPanel.addStyleName(wrapperClassName);

    String visConfigName = vi.getMappings().get("config");
    String hitMarkConfig = vi.getMappings().getOrDefault("hitmark", "true");
    hitMark = Boolean.parseBoolean(hitMarkConfig);
    mc = vi.getMarkedAndCovered();

    VisualizationDefinition[] definitions =
        parseDefinitions(corpusName, rootCorpusId, vi.getMappings(), vi.getUI());
    log.debug("Parsed {} htmlvis definitions for path {}", definitions.length,
        corpusPath.toArray());

    if (definitions != null) {

      lblResult.setValue(createHTML(vi.getSResult().getDocumentGraph(), definitions, vi.getUI()));

      String labelClass = vi.getMappings().getOrDefault("class", "htmlvis");
      lblResult.addStyleName(labelClass);

      injectWebFonts(visConfigName, corpusName, rootCorpusId, vi.getUI(),
          new CorporaApi(Helper.getClient(vi.getUI())));
      injectCSS(visConfigName, corpusName, rootCorpusId, wrapperClassName, vi.getUI());
    }

    if (vi.getMappings().containsKey("debug")) {
      TextArea txtDebug = new TextArea();
      txtDebug.setValue(lblResult.getValue());
      txtDebug.setReadOnly(true);
      txtDebug.setWidth("100%");
      Label sep = new Label("<hr/>", ContentMode.HTML);
      VerticalLayout layout = new VerticalLayout(txtDebug, sep, lblResult);
      layout.setSizeUndefined();
      scrollPanel.setContent(layout);
    } else {
      scrollPanel.setContent(lblResult);
    }

    return scrollPanel;
  }

  public String createHTML(SDocumentGraph graph, VisualizationDefinition[] definitions, UI ui) {
    HashMap<VisualizationDefinition, Integer> instruction_priorities = new HashMap<>();

    SortedMap<Long, List<OutputItem>> outputStartTags = new TreeMap<>();
    SortedMap<Long, List<OutputItem>> outputEndTags = new TreeMap<>();
    StringBuilder sb = new StringBuilder();

    List<SToken> token = graph.getSortedTokenByText();

    Map<SToken, Long> token2index = new HashMap<>();
    {
      long i = 0;
      for (SToken t : token) {
        token2index.put(t, i++);
      }
    }
    // Get metadata for visualizer if stylesheet requires it
    // First check the stylesheet
    Boolean bolMetaTypeFound = false;

    HashMap<String, String> meta = new HashMap<>();
    int def_priority = 0;
    for (VisualizationDefinition vis : definitions) {
      if (vis.getOutputter().getType() == SpanHTMLOutputter.Type.META_NAME) {
        bolMetaTypeFound = true;
      } else // not a meta-annotation, remember order in config file to set priority
      {
        if (vis.getMatcher() instanceof AnnotationNameMatcher) {
          instruction_priorities.put(vis, def_priority);
        } else if (vis.getMatcher() instanceof AnnotationNameAndValueMatcher) {
          instruction_priorities.put(vis, def_priority);
        } else if (vis.getMatcher() instanceof TokenMatcher) {
          instruction_priorities.put(vis, def_priority);
        }
        def_priority--;
      }
      vis.getOutputter().setMeta(meta);

    }
    if (bolMetaTypeFound == true) // Metadata is required, get corpus and document name
    {
      // Get corpus and document name
      String strDocName = "";
      String strCorpName = "";
      strDocName = graph.getDocument().getName();
      List<String> corpusPath =
          Helper.getCorpusPath(graph.getDocument().getGraph(), graph.getDocument());
      strCorpName = corpusPath.get(corpusPath.size() - 1);

      // Get metadata and put in hashmap
      List<SMetaAnnotation> metaData = Helper.getMetaDataDoc(strCorpName, strDocName, ui);
      for (SMetaAnnotation metaDatum : metaData) {
        meta.put(metaDatum.getName(), metaDatum.getValue_STEXT());
      }
    }

    for (SToken t : token) {
      tokenColor = "";
      if (mc.containsKey(t) && hitMark) {
        tokenColor = MatchedNodeColors.getHTMLColorByMatch(mc.get(t));
      }
      for (VisualizationDefinition vis : definitions) {
        String matched = vis.getMatcher().matchedAnnotation(t);
        if (matched != null) {
          vis.getOutputter().outputHTML(t, matched, outputStartTags, outputEndTags, tokenColor,
              instruction_priorities.getOrDefault(vis, 0), token2index);
        }
      }
    }

    List<SSpan> spans = graph.getSpans();
    if (spans != null && !spans.isEmpty()) {
      for (VisualizationDefinition vis : definitions) {

        for (SSpan span : spans) {
          tokenColor = "";
          if (mc.containsKey(span) && hitMark) {
            tokenColor = MatchedNodeColors.getHTMLColorByMatch(mc.get(span));
          }
          String matched = vis.getMatcher().matchedAnnotation(span);
          if (matched != null) {
            vis.getOutputter().outputHTML(span, matched, outputStartTags, outputEndTags, tokenColor,
                instruction_priorities.getOrDefault(vis, 0), token2index);
          }
        }
      }
    } // end if spans not empty

    int minStartTagPos = outputStartTags.firstKey().intValue();
    int maxEndTagPos = outputEndTags.lastKey().intValue();

    // Find BEGIN and END instructions if available
    for (VisualizationDefinition vis : definitions) {

      if (vis.getMatcher() instanceof PseudoRegionMatcher) {
        PseudoRegionMatcher.PseudoRegion psdRegionType =
            ((PseudoRegionMatcher) vis.getMatcher()).getPsdRegion();
        int positionStart = 0;
        int positionEnd = 0;

        if (!outputEndTags.isEmpty() && !outputStartTags.isEmpty() && psdRegionType != null) {
          switch (psdRegionType) {
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

        switch (vis.getOutputter().getType()) {
          case META_NAME:
            String strMetaVal = meta.get(vis.getOutputter().getMetaname().trim());
            if (strMetaVal == null) {
              throw new NullPointerException("no such metadata name in document: '"
                  + vis.getOutputter().getMetaname().trim() + "'");
            } else {
              vis.getOutputter().outputAny(positionStart, positionEnd,
                  ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(), strMetaVal,
                  outputStartTags, outputEndTags, instruction_priorities.getOrDefault(vis, 0));
            }
            break;
          case CONSTANT:
            vis.getOutputter().outputAny(positionStart, positionEnd,
                ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(),
                vis.getOutputter().getConstant(), outputStartTags, outputEndTags,
                instruction_priorities.getOrDefault(vis, 0));
            break;
          case EMPTY:
            vis.getOutputter().outputAny(positionStart, positionEnd,
                ((PseudoRegionMatcher) vis.getMatcher()).getAnnotationName(), "", outputStartTags,
                outputEndTags, instruction_priorities.getOrDefault(vis, 0));
            break;
          case ANNO_NAME:
            break; // this shouldn't happen, since the BEGIN/END instruction has no triggering
                   // annotation name or value
          case VALUE:
            break; // this shouldn't happen, since the BEGIN/END instruction has no triggering
                   // annotation name or value
          case ESCAPED_VALUE:
            break; // this shouldn't happen, since the BEGIN/END instruction has no triggering
                   // annotation name or value
          default:
        }

      }
    }

    // get all used indexes
    Set<Long> indexes = new TreeSet<>();
    indexes.addAll(outputStartTags.keySet());
    indexes.addAll(outputEndTags.keySet());

    for (Long i : indexes) {
      // output all strings belonging to this token position
      // first the start tags for this position

      // add priorities from instruction_priorities for sorting length ties
      List<OutputItem> unsortedStart = outputStartTags.get(i);
      SortedSet<OutputItem> itemsStart = new TreeSet();
      if (unsortedStart != null) {
        Iterator<OutputItem> it = unsortedStart.iterator();
        while (it.hasNext()) {
          OutputItem s = it.next();
          itemsStart.add(s);
        }
      }

      {
        Iterator<OutputItem> it = itemsStart.iterator();
        boolean first = true;
        while (it.hasNext()) {
          OutputItem s = it.next();
          if (!first) {
            sb.append("-->");
          }
          first = false;
          sb.append(s.getOutputString());
          if (it.hasNext()) {
            sb.append("<!--\n");
          }
        }
      }
      // then the end tags for this position, but inverse their order
      List<OutputItem> unsortedEnd = outputEndTags.get(i);
      SortedSet<OutputItem> itemsEnd = new TreeSet();
      if (unsortedEnd != null) {
        Iterator<OutputItem> it = unsortedEnd.iterator();
        while (it.hasNext()) {
          OutputItem s = it.next();
          itemsEnd.add(s);
        }
      }

      {
        List<OutputItem> itemsEndReverse = new LinkedList<>(itemsEnd);
        Collections.reverse(itemsEndReverse);
        for (OutputItem s : itemsEndReverse) {
          sb.append(s.getOutputString());
        }
      }

    }


    return sb.toString();
  }

  private JsonMapper createJsonMapper() {
    JsonMapper jsonMapper = new JsonMapper();
    jsonMapper.registerModule(new JaxbAnnotationModule());
    // the json should be human readable
    jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return jsonMapper;
  }

  @Override
  public List<String> getFilteredNodeAnnotationNames(String toplevelCorpusName,
      String toplevelCorpusId, String documentName, Map<String, String> mappings, UI ui) {
    Set<String> result = null;

    toplevelCorpusId = Helper.removeSaltPrefix(toplevelCorpusId);

    VisualizationDefinition[] definitions =
        parseDefinitions(toplevelCorpusName, toplevelCorpusId, mappings, ui);

    if (definitions != null) {
      for (VisualizationDefinition def : definitions) {
        List<String> sub = def.getMatcher().getRequiredAnnotationNames();
        if (sub == null) {
          // a rule requires all annotations, abort
          result = null;
          break;
        } else {
          if (result == null) {
            result = new LinkedHashSet<>();
          }
          result.addAll(sub);
        }
      }
    }

    if (result == null) {
      return null;
    } else {
      return new LinkedList<>(result);
    }
  }

  @Override
  public String getShortName() {
    return "html";
  }

  private void injectCSS(String visConfigName, String corpusName, String corpusNodeId,
      String wrapperClassName, UI ui) {
    CorporaApi api = new CorporaApi(Helper.getClient(ui));
    InputStream inStreamCSSRaw = null;
    if (visConfigName == null) {
      inStreamCSSRaw = HTMLVis.class.getResourceAsStream("htmlvis.css");
    } else {
      try {
        File f = api.getFile(corpusName, corpusNodeId + "/" + visConfigName + ".css");
        f.deleteOnExit();

        inStreamCSSRaw = new FileInputStream(f);

      } catch (ApiException ex) {
        if (ex.getCode() != 404) {
          log.error("Could not retrieve the HTML visualizer web-font configuration file", ex);
          ui.access(() -> {
            Notification.show("Could not retrieve the HTML visualizer web-font configuration file",
                ex.getMessage(), Notification.Type.ERROR_MESSAGE);

          });
        }
      } catch (FileNotFoundException ex) {
        log.error("Just downloaded file not found", ex);
      }

    }
    if (inStreamCSSRaw != null) {
      try (InputStream inStreamCSS = inStreamCSSRaw) {
        String cssContent = IOUtils.toString(inStreamCSS);
        if (ui instanceof AnnisBaseUI) {
          // do not add identical CSS files
          ((AnnisBaseUI) ui).injectUniqueCSS(cssContent, wrapperClassName);
        }
      } catch (IOException ex) {
        log.error("Could not parse the HTML visualizer CSS file", ex);
        Notification.show("Could not parse the HTML visualizer CSS file", ex.getMessage(),
            Notification.Type.ERROR_MESSAGE);
      }
    }
  }

  protected void injectWebFonts(String visConfigName, String corpusName, String corpusNodeId, UI ui,
      CorporaApi api) {

    try {
      File f = api.getFile(corpusName, corpusNodeId + "/" + visConfigName + ".fonts.json");
      f.deleteOnExit();
      try (FileInputStream inStreamJSON = new FileInputStream(f)) {
        ObjectMapper mapper = createJsonMapper();
        WebFontList fontConfigList = mapper.readValue(inStreamJSON, WebFontList.class);

        for (WebFont fontConfig : fontConfigList.getWebFonts()) {
          injectWebFontConfig(fontConfig, ui);
        }

      } catch (IOException ex) {
        log.error("Could not parse the HTML visualizer web-font configuration file", ex);
        new Notification("Could not parse the HTML visualizer web-font configuration file",
            ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(ui.getPage());
      } finally {
        Files.deleteIfExists(f.toPath());
      }
    } catch (IOException ex) {
      log.error("Unexpected input/output exception", ex);
    } catch (ApiException ex) {
      if (ex.getCode() != 404) {
        log.error("Could not retrieve the HTML visualizer web-font configuration file", ex);
        ui.access(() -> {
          new Notification("Could not retrieve the HTML visualizer web-font configuration file",
              ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(ui.getPage());

        });
      }
    }
  }

  private void injectWebFontConfig(WebFont fontConfig, UI ui) {
    if (fontConfig != null && fontConfig.getName() != null) {
      StringBuilder sb = new StringBuilder();

      sb.append("@font-face {\n");
      sb.append("  font-family: '" + fontConfig.getName() + "';\n");
      sb.append("  font-weight: '" + fontConfig.getWeight() + "';\n");
      sb.append("  font-style: '" + fontConfig.getStyle() + "';\n");

      List<String> sourceDefs = new LinkedList<>();
      for (Map.Entry<String, String> src : fontConfig.getSources().entrySet()) {
        sourceDefs.add("url('" + src.getValue() + "') format('" + src.getKey() + "')");
      }

      if (!sourceDefs.isEmpty()) {
        sb.append("  src: ");
        sb.append(Joiner.on(",\n    ").join(sourceDefs));
        sb.append(";\n");
      }

      sb.append("}\n");

      if (ui instanceof AnnisBaseUI) {
        // do not add identical CSS files
        ((AnnisBaseUI) ui).injectUniqueCSS(sb.toString());
      }
    }
  }

  @Override
  public boolean isUsingText() {
    return false;
  }

  private VisualizationDefinition[] parseDefinitions(String corpusName, String corpusNodeId,
      Map<String, String> mappings, UI ui) {
    InputStream inStreamConfigRaw = null;

    String visConfigName = mappings.get("config");

    if (visConfigName == null) {
      inStreamConfigRaw = HTMLVis.class.getResourceAsStream("defaultvis.config");
    } else {

      CorporaApi api = new CorporaApi(Helper.getClient(ui));
      try {
        File file = api.getFile(corpusName, corpusNodeId + "/" + visConfigName + ".config");
        inStreamConfigRaw = new FileInputStream(file);
      } catch (ApiException e) {
        if (e.getCode() != 404) {
          log.error("Exception while getting the HTML visualizer configuration", e);
        }
      } catch (IOException e) {
        log.error("Exception while getting the HTML visualizer configuration", e);
      }
    }

    if (inStreamConfigRaw == null) {
      ui.accessSynchronously(() -> Notification.show(
          "ERROR: html visualization configuration \"" + visConfigName + "\" not found in database",
          Notification.Type.ERROR_MESSAGE));
    } else {

      try (InputStream inStreamConfig = inStreamConfigRaw) {

        VisParser p = new VisParser(inStreamConfig);
        return p.getDefinitions();
      } catch (IOException | VisParserException ex) {
        log.error("Could not parse the HTML visualization configuration file", ex);

        ui.accessSynchronously(
            () -> Notification.show("Could not parse the HTML visualization configuration file",
                ex.getMessage(), Notification.Type.ERROR_MESSAGE));

      }
    }
    return null;
  }

}
