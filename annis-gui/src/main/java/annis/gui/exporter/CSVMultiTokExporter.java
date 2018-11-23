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
package annis.gui.exporter;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.model.AnnisConstants;
import annis.model.Annotation;
import annis.model.RelannisNodeFeature;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import annis.service.objects.SubgraphFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;

/**
 * A csv-exporter that will export the text of the underlying token
 * instead of the base text.
 * This is useful for getting text spans where the normal csv-exporter
 * doesn't work since there are multiple speakers or normalizations.
 *
 * @author Fabian Barteld
 */
@PluginImplementation
public class CSVMultiTokExporter extends SaltBasedExporter
{
  @Override
  public String getHelpMessage()
  {
    return "The CSV MultiTok Exporter exports only the "
      + "values of the elements searched for by the user, ignoring the context "
      + "around search results. The values for all annotations of each of the "
      + "found nodes is given in a comma-separated table (CSV). <br/><br/>"
      + "This exporter will take more time than the normal CSV Exporter "
      + "but it is able to export the underlying text for spans "
      + "if the corpus contains multiple tokenizations. <br/><br/>"
      + "Parameters: <br/>"
      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
      + "<code>metakeys=title,documentname</code>)";
  }

  @Override
  public SubgraphFilter getSubgraphFilter()
  {
    return SubgraphFilter.all;
  }

  @Override
  public String getFileEnding()
  {
    return "csv";
  }

  private Set<String> metakeys;
  private SortedMap<Integer,TreeSet<String>> annotationsForMatchedNodes;

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

    for (SNode node: graph.getNodes()) {
       if (node.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE) != null)
         matchedNodes.add(node);
    }

    return matchedNodes;
  }

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
  public void createAdjacencyMatrix(SDocumentGraph graph,
    Map<String, String> args, int matchNumber, int nodeCount) throws IOException, IllegalArgumentException
  {
    // first match
    if (matchNumber == 0)
    {
      // get list of metakeys to export
      metakeys = new HashSet<>();
      if (args.containsKey("metakeys"))
      {
        metakeys.addAll(Arrays.asList(args.get("metakeys").split(",")));
      }
      // initialize list of annotations for the matched nodes
      annotationsForMatchedNodes = new TreeMap<>();
    }
    for (SNode node: this.getMatchedNodes(graph)) {
      int node_id = node
          .getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE)
          .getValue_SNUMERIC().intValue();
      if(!annotationsForMatchedNodes.containsKey(node_id))
        annotationsForMatchedNodes.put(node_id, new TreeSet<String>());
      List<SAnnotation> annots = new ArrayList<>(node.getAnnotations());
      Set<String> annoNames = annotationsForMatchedNodes.get(node_id);
      for (SAnnotation annot: annots) {
        annoNames.add(annot.getNamespace() + "::" + annot.getName());
      }
    }
  }

  /**
   * Takes a match and outputs a csv-line
   *
   * @param graph
   * @param alignmc
   * @param matchNumber
   * @param out
   *
   * @throws java.io.IOException
   *
   */
  @Override
  public void outputText(SDocumentGraph graph, boolean alignmc, int matchNumber,
    Writer out) throws IOException, IllegalArgumentException
  {

    // first match
    if (matchNumber == 0)
    {
      // output header
      List<String> headerLine = new ArrayList<>();
      for(Map.Entry<Integer, TreeSet<String>> match: annotationsForMatchedNodes.entrySet())
      {
        int node_id = match.getKey();
        headerLine.add(String.valueOf(node_id) + "_id");
        headerLine.add(String.valueOf(node_id) + "_span");
        for (String annoName: match.getValue())
        {
          headerLine.add(String.valueOf(node_id) + "_anno_" + annoName);
        }
      }
      for (String key: metakeys) {
        headerLine.add("meta_" + key);
      }
      out.append(StringUtils.join(headerLine, "\t"));
      out.append("\n");
    }

    // output nodes in the order of the matches
    SortedMap<Integer, String> contentLine = new TreeMap<>();
    for (SNode node: this.getMatchedNodes(graph)) {
      List<String> nodeLine = new ArrayList<>();
      // export id
      RelannisNodeFeature feats = RelannisNodeFeature.extract(node);
      nodeLine.add(String.valueOf(feats.getInternalID()));
      // export spanned text
      String span = graph.getText(node);
      if (span != null)
        nodeLine.add(graph.getText(node));
      else
        nodeLine.add("");
      // export annotations
      int node_id = node
        .getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE)
        .getValue_SNUMERIC().intValue();
      for (String annoName: annotationsForMatchedNodes.get(node_id))
      {
        SAnnotation anno = node.getAnnotation(annoName);
        if (anno != null)
        {
          nodeLine.add(anno.getValue_STEXT());
        }
        else
          nodeLine.add("'NULL'");
      }
      // add everything to line
      contentLine.put(node_id, StringUtils.join(nodeLine, "\t"));
    }

    out.append(StringUtils.join(contentLine.values(), "\t"));

    // export Metadata
    // TODO cache the metadata
    if(!metakeys.isEmpty()) {
      // TODO is this the best way to get the corpus name?
      String corpus_name = CommonHelper.getCorpusPath(java.net.URI.create(graph.getDocument().getId())).get(0);
      List<Annotation> asList = Helper.getMetaData(corpus_name, graph.getDocument().getName());
      for(Annotation anno : asList)
      {
        if (metakeys.contains(anno.getName()))
          out.append("\t" + anno.getValue());
      }
    }

    out.append("\n");
  }

  @Override
  public void getOrderedMatchNumbers()
  {
    // TODO
  }

  @Override
  public boolean isAlignable()
  {
    return false;
  }
}
