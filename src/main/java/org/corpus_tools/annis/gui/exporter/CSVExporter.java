/*
 * Copyright 2016-2017 Referenzkorpus Mittelniederdeutsch/Niederrheinisch (1200-1650).
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
package org.corpus_tools.annis.gui.exporter;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.objects.AnnisConstants;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.springframework.stereotype.Component;

/**
 * A csv-exporter that will export the text of the underlying token instead of
 * the base text. This is useful for getting text spans where the normal
 * csv-exporter doesn't work since there are multiple speakers or
 * normalizations.
 *
 * @author Fabian Barteld
 */
@Component
public class CSVExporter extends BaseMatrixExporter {
    /**
     * 
     */
    private static final long serialVersionUID = -8993537374171042122L;

    private Set<String> metakeys;

    private SortedMap<Integer, TreeSet<String>> annotationsForMatchedNodes;

    /**
     * Takes a match and stores annotation names to construct the header in
     * {@link #outputText(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph, boolean, int, java.io.Writer) }
     *
     * @param graph
     * @param args
     * @param matchNumber
     * @param nodeCount
     *
     * @throws java.io.IOException
     *
     */
    @Override
    public void createAdjacencyMatrix(SDocumentGraph graph, Map<String, String> args, int matchNumber, int nodeCount)
            throws IOException, IllegalArgumentException {
        // first match
        if (matchNumber == 0) {
            // get list of metakeys to export
            metakeys = new LinkedHashSet<>();
            if (args.containsKey("metakeys")) {
                metakeys.addAll(Arrays.asList(args.get("metakeys").split(",")));
            }
            // initialize list of annotations for the matched nodes
            annotationsForMatchedNodes = new TreeMap<>();
        }
        for (SNode node : this.getMatchedNodes(graph)) {
            int node_id = node.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE).getValue_SNUMERIC()
                    .intValue();
            if (!annotationsForMatchedNodes.containsKey(node_id))
                annotationsForMatchedNodes.put(node_id, new TreeSet<String>());
            List<SAnnotation> annots = new ArrayList<>(node.getAnnotations());
            Set<String> annoNames = annotationsForMatchedNodes.get(node_id);
            for (SAnnotation annot : annots) {
                annoNames.add(annot.getNamespace() + "::" + annot.getName());
            }
        }
    }

    @Override
    public String getFileEnding() {
        return "csv";
    }

    @Override
    public String getHelpMessage() {
        return "The CSV Exporter exports only the "
                + "values of the elements searched for by the user, ignoring the context " + "around search results. "
                + "The values for all annotations of each of the "
                + "found nodes is given in a comma-separated table (CSV). <br/><br/>" + "Parameters: <br/>"
                + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
            + "<code>metakeys=title,documentname</code>)<br/>"
            + "<em>segmentation</em> - For corpora with multiple segmentation (e.g. dialog corpora) use this segmentation for getting the span of the matched nodes.";
    }

    /**
     * Takes a match and returns the matched nodes.
     *
     * @param graph
     *
     * @throws java.io.IOException
     *
     */
    private Set<SNode> getMatchedNodes(SDocumentGraph graph) {

        Set<SNode> matchedNodes = new HashSet<>();

        for (SNode node : graph.getNodes()) {
            if (node.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE) != null)
                matchedNodes.add(node);
        }

        return matchedNodes;
    }

    @Override
    public void getOrderedMatchNumbers() {
      // TODO
    }

    @Override
    public boolean isAlignable() {
        return false;
    }

    @Override
    public boolean needsContext() {
        return false;
    }

    private List<String> createHeaderLine() {
      List<String> headerLine = new ArrayList<>();
      for (Map.Entry<Integer, TreeSet<String>> match : annotationsForMatchedNodes.entrySet()) {
        int node_id = match.getKey();
        headerLine.add(String.valueOf(node_id) + "_id");
        headerLine.add(String.valueOf(node_id) + "_span");
        for (String annoName : match.getValue()) {
          headerLine.add(String.valueOf(node_id) + "_anno_" + annoName);
        }
      }
      for (String key : metakeys) {
        headerLine.add("meta_" + key);
      }
      return headerLine;
    }

    private SortedMap<Integer, String> createLineForNodes(SDocumentGraph graph,
        Map<String, String> args) {
      SortedMap<Integer, String> contentLine = new TreeMap<>();
      for (SNode node : this.getMatchedNodes(graph)) {
          List<String> nodeLine = new ArrayList<>();
          nodeLine.add(node.getId());
          // export spanned text
          String span = getSpannedText(graph, node, args.get(ExportHelper.SEGMENTATION_KEY));
          if (span != null)
            nodeLine.add(span);
          else
              nodeLine.add("");
          // export annotations
          int node_id = node.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE).getValue_SNUMERIC()
                  .intValue();
          for (String annoName : annotationsForMatchedNodes.get(node_id)) {
              SAnnotation anno = node.getAnnotation(annoName);
              if (anno != null) {
                  nodeLine.add(anno.getValue_STEXT());
              } else
                  nodeLine.add("'NULL'");
          }
          // add everything to line
          contentLine.put(node_id, StringUtils.join(nodeLine, "\t"));
      }
      return contentLine;
    }

    private void appendMetaLine(SDocumentGraph graph, Writer out, UI ui) throws IOException {
      // TODO is this the best way to get the corpus name?
      String corpusName = Helper.getCorpusPath(graph.getDocument().getId()).get(0);
      // TODO cache the metadata
      List<SMetaAnnotation> metaAnnos = new ArrayList<>();
      for(SNode n : Helper.getMetaData(corpusName, Optional.of(graph.getDocument().getName()), ui).getNodes()) {
        metaAnnos.addAll(n.getMetaAnnotations());
      }
      Multimap<String, SMetaAnnotation> metaAnnosByName = Multimaps.index(metaAnnos, SMetaAnnotation::getName);

      for (String metaName : metakeys) {
        Collection<SMetaAnnotation> annos = metaAnnosByName.get(metaName);
        if (annos == null || annos.isEmpty()) {
          out.append("\t");
        } else
          out.append(
              "\t" + annos.stream().map(SMetaAnnotation::getValue_STEXT)
                  .collect(Collectors.joining(", ")));
      }
    }

    /**
     * Takes a match and outputs a csv-line
     *
     * @param graph
     * @param args
     * @param alignmc
     * @param matchNumber
     * @param out
     *
     * @throws java.io.IOException
     *
     */
    @Override
    public void outputText(SDocumentGraph graph, Map<String, String> args, boolean alignmc,
        int matchNumber, Writer out, UI ui)
            throws IOException, IllegalArgumentException {

        // first match
        if (matchNumber == 0) {
            // output header
            out.append(StringUtils.join(createHeaderLine(), "\t"));
            out.append("\n");
        }

        // output nodes in the order of the matches
        SortedMap<Integer, String> contentLine = createLineForNodes(graph, args);
        out.append(StringUtils.join(contentLine.values(), "\t"));

        // export Metadata
        if (!metakeys.isEmpty()) {
          appendMetaLine(graph, out, ui);
        }

        out.append("\n");
    }

    private String getSpannedText(SDocumentGraph graph, SNode node, String segmentation) {
       if(segmentation == null || segmentation.isEmpty()) {
         return graph.getText(node);
       } else {
         // Filter out the nodes that cover the same range as our matched node
         List<SNode> segmentationNodes = Helper.getSortedSegmentationNodes(segmentation, graph).stream().filter(segNode -> {
           if(segNode.equals(node)) {
             return true;
           } else {
             // Get covered token of both nodes and check if they overlap
             Set<SToken> coveredToken = new HashSet<>(graph.getOverlappedTokens(node));
             return graph.getOverlappedTokens(segNode).parallelStream()
                 .anyMatch(coveredToken::contains);
           }
         }).collect(Collectors.toList());

         return segmentationNodes.stream().map(n -> n.getFeature("annis::tok"))
             .filter(Objects::nonNull)
             .map(a -> a.getValue().toString()).collect(Collectors.joining(" "));
       }
    }
}
