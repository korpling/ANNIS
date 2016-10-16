package annis.gui.exporter;

import annis.model.AnnisConstants;
import annis.model.RelannisNodeFeature;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import annis.service.objects.SubgraphFilter;
import java.util.ArrayList;
import java.util.Comparator;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
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
      + "if the corpus contains multiple tokenizations. <br/><br/>";
// // TODO output metadata
//      + "Parameters: <br/>"
//      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
//      + "<code>metakeys=title,documentname</code>)";
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
  
  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber,
    Writer out) throws IOException
  {
    // get number of match nodes
    int number_of_matched_nodes = graph
      .getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS)
      .getValue_STEXT().split(",").length;

    // collect matched nodes
    SNode[] matches = new SNode[number_of_matched_nodes];
    for(SNode node: graph.getNodes())
    {
      SFeature match_number = node.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
      if(match_number != null)
      {
        matches[match_number.getValue_SNUMERIC().intValue()-1] = node;
      }
    }
    // first match - output header
    // TODO should iterate over all matches to compute the header
    if (matchNumber == -1) {
      List<String> headerLine = new ArrayList<>();
      for (int i=0; i < matches.length; i++) {
        headerLine.add(String.valueOf(i+1) + "_id");
        headerLine.add(String.valueOf(i+1) + "_span");
        List<SAnnotation> annots = new ArrayList<>(matches[i].getAnnotations());
        java.util.Collections.sort(annots, new Comparator<SAnnotation>() {
          @Override
          public int compare(SAnnotation a, SAnnotation b)
          {
            return a.getName().compareTo(b.getName());
          }
        });
        for (SAnnotation annot: annots)
        {
          headerLine.add(String.valueOf(i+1) + "_anno_"
            + annot.getNamespace() + "::" + annot.getName());
        }
      }
    out.append(StringUtils.join(headerLine, "\t"));
    out.append("\n");
    }
    
    // output nodes in the order of the matches
    List<String> contentLine = new ArrayList<>();
    for (SNode node: matches) {
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
      List<SAnnotation> annots = new ArrayList<>(node.getAnnotations());
      java.util.Collections.sort(annots, new Comparator<SAnnotation>() {
        @Override
        public int compare(SAnnotation a, SAnnotation b)
        {
          return a.getName().compareTo(b.getName());
        }
      });
      for (SAnnotation annot: annots)
      {
        contentLine.add(annot.getValue_STEXT());
      }
    }
    out.append(StringUtils.join(contentLine, "\t"));
    out.append("\n");
  }
}