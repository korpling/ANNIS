package annis.gui.exporter;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.model.AnnisConstants;
import annis.model.Annotation;
import annis.model.RelannisNodeFeature;
import annis.service.objects.CorpusConfig;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import annis.service.objects.SubgraphFilter;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.WebResource;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;

/**
 * @author barteld
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
  private List<TreeSet<String>> annotationsForMatchedNodes;

  private boolean firstIteration;

  @Override
  public Exception convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, List<String> keys, String argsAsString,
    WebResource annisResource, Writer out, EventBus eventBus, Map<String, CorpusConfig> corpusConfigs)
  {
    this.firstIteration = true;
    Writer nullWriter = new OutputStreamWriter(
        new OutputStream()
        {
          @Override
          public void write(int b)
          {}
        });
    Exception ex = super.convertText(queryAnnisQL, contextLeft, contextRight, corpora,
      keys, argsAsString, annisResource,
      nullWriter,  // avoid empty line at the beginning
      null,  // don't show export update during the first iteration
      corpusConfigs);
    if (ex != null)
      return ex;
    this.firstIteration = false;
    return super.convertText(queryAnnisQL, contextLeft, contextRight, corpora,
      keys, argsAsString, annisResource, out, eventBus, corpusConfigs);
  }

  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber,
    Writer out) throws IOException
  {
    if(this.firstIteration)
      this.collectHeader(graph, annoKeys, args, matchNumber, out);
    else
      this.writeText(graph, annoKeys, args, matchNumber, out);
  }

  private void collectHeader(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber,
    Writer out) throws IOException
  {
    // first match
    if (matchNumber == -1)
    {
      // get list of metakeys to export
      metakeys = new HashSet<>();
      if (args.containsKey("metakeys"))
      {
        metakeys.addAll(Arrays.asList(args.get("metakeys").split(",")));
      }
      // initialize list of annotations for the matched nodes
      annotationsForMatchedNodes = new ArrayList<>();
    }
    // get list of annotations for the nodes in the current match
    String[] matchIDs = graph
      .getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS)
      .getValue_STEXT().split(",");
    for (int i=0; i < matchIDs.length; i++)
    {
      if(i >= annotationsForMatchedNodes.size())
        annotationsForMatchedNodes.add(i, new TreeSet<String>());
      List<SAnnotation> annots = new ArrayList<>(graph.getNode(matchIDs[i]).getAnnotations());
      Set<String> annoNames = annotationsForMatchedNodes.get(i);
      for (SAnnotation annot: annots) {
        annoNames.add(annot.getNamespace() + "::" + annot.getName());
      }
    }
  }
  private void writeText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber,
    Writer out) throws IOException
  {

    // first match
    if (matchNumber == -1)
    {
      // output header
      List<String> headerLine = new ArrayList<>();
      for(int i=0; i < annotationsForMatchedNodes.size(); i++)
      {
        headerLine.add(String.valueOf(i+1) + "_id");
        headerLine.add(String.valueOf(i+1) + "_span");
        for (String annoName: annotationsForMatchedNodes.get(i))
        {
          headerLine.add(String.valueOf(i+1) + "_anno_" + annoName);
        }
      }
      for (String key: metakeys) {
        headerLine.add("meta_" + key);
      }
      out.append(StringUtils.join(headerLine, "\t"));
      out.append("\n");
    }

    // output nodes in the order of the matches
    List<String> contentLine = new ArrayList<>();
    String[] matchIDs = graph
      .getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS)
      .getValue_STEXT().split(",");
    for (int i=0; i < matchIDs.length; i++)
    {
      String matchid = matchIDs[i];
      SNode node = graph.getNode(matchid);
      // export id
      RelannisNodeFeature feats = RelannisNodeFeature.extract(node);
      contentLine.add(String.valueOf(feats.getInternalID()));     
      // export spanned text
      String span = graph.getText(node);
      if (span != null)
        contentLine.add(graph.getText(node));
      else
        contentLine.add("");
      // export annotations
      for (String annoName: annotationsForMatchedNodes.get(i))
      {
        SAnnotation anno = node.getAnnotation(annoName);
        if (anno != null)
          contentLine.add(anno.getValue_STEXT());
        else
          contentLine.add("'NULL'");
      }
    }
    // export Metadata
    // TODO cache the metadata
    if(!metakeys.isEmpty()) {
      // TODO is this the best way to get the corpus name?
      String corpus_name = CommonHelper.getCorpusPath(java.net.URI.create(graph.getDocument().getId())).get(0);
      List<Annotation> asList = Helper.getMetaData(corpus_name, graph.getDocument().getName());
      for(Annotation anno : asList)
      {
        if (metakeys.contains(anno.getName()))
          contentLine.add(anno.getValue());
      }
    }

    out.append(StringUtils.join(contentLine, "\t"));
    out.append("\n");
  }
}