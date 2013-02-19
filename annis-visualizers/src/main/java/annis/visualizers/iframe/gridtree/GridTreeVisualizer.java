package annis.visualizers.iframe.gridtree;

import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.iframe.WriterVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Known Bug: the visualizer does not handle crossing edges.
 * {@linkplain https://github.com/korpling/ANNIS/issues/14}
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmailcom>
 *
 */
@PluginImplementation
public class GridTreeVisualizer extends WriterVisualizer
{

  // looking for the configuration in resolver_vis_map
  final private String PROPERTY_KEY = "node_key";
  private VisualizerInput input;

  /**
   * This helper-class saves the span from a specific Node. The span is
   * represented as tokenIndex from the most left and the most right Token of
   * the root.
   *
   * @author benjamin
   *
   */
  private class Span implements Comparable<Span>
  {

    Long left;
    Long right;
    AnnisNode root;
    String anno;
    int height;
    long offset;

    /**
     * left and right should be initiate with null, when root is not a token.
     *
     * @param root
     * @param offset the tokenIndex of the first element in token
     * @param length the length of token must be a sorted List of the result
     */
    public Span(AnnisNode root, long offset, int length, String anno)
    {
      this.root = root;
      this.offset = offset;
      this.anno = anno;
      left = (root.getLeftToken() < offset) ? offset : root.getLeftToken();
      right = (root.getRightToken() > offset + length) ? offset + length : root.getRightToken();
      calculateHeight(root, 0);
    }

    @Override
    /**
     * this function assumes the spans doesn't have conflicts which means that:
     * <br /> sp.height == this.height => [sp.left, sp.right] &cap; [this.left,
     * sp.right] == &empty;
     *
     *
     */
    public int compareTo(Span sp)
    {
      if (this.height > sp.height)
      {
        return 1;
      }
      if (this.height == sp.height)
      {
        if (this.left > sp.right)
        {
          return 1;
        }
        else
        {
          return -1;
        }
      }
      return -1;
    }

    private void calculateHeight(AnnisNode current, int height)
    {

      if (current != null)
      {
        for (Edge incoming : current.getIncomingEdges())
        {
          AnnisNode tmp = incoming.getSource();

          if (hasAnno(tmp, anno))
          {
            calculateHeight(tmp, height + 1);
          }

          calculateHeight(tmp, height);
        }
      }

      this.height = Math.max(this.height, height);
    }

    @Override
    public boolean equals(Object obj)
    {
      return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
      return super.hashCode();
    }

    public void colspan(StringBuilder sb, String anno)
    {

      String annoValue = getAnnoValue(this.root, anno);

      sb.append("<td colspan=\"");
      sb.append(Math.abs(this.right - this.left) + 1);

      // cell-index for hover-effect
      sb.append("\" id=\"intervall:");
      sb.append(this.hashCode());
      sb.append(":");

      sb.append(left + 1 - offset);
      sb.append("-");
      sb.append(right + 1 - offset);

      sb.append("\" title=\"");
      sb.append(anno);
      sb.append("=");
      sb.append(annoValue);

      sb.append("\" class=\"gridtree-result\" ");
      sb.append("style=\"color:").append(input.getMarkableExactMap().get(Long.toString(this.root.getId()))).append("\">");
      sb.append(annoValue);
      sb.append("</td>");
    }

    public boolean isInIntervall(long l)
    {
      if (this.left <= l && l <= this.right)
      {
        return true;
      }
      return false;
    }
  }

  @Override
  public String getShortName()
  {
    return "grid_tree";
  }

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    ArrayList<Span> spans = new ArrayList<GridTreeVisualizer.Span>();
    this.input = input;

    try
    {
      writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");

      writer.append("<link href=\""
        + input.getResourcePath("jquery.tooltip.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\"" + input.getResourcePath("partitur.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\"" + input.getResourcePath("gridtree.css")
        + "\" rel=\"stylesheet\" type=\"text/css\" >");

      writer.append("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("jquery-1.6.2.min.js") + "\"></script>");
      writer.append("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("jquery.tooltip.min.js") + "\"></script>");

      writer.append("<script type=\"text/javascript\" src=\""
        + input.getResourcePath("gridtreeVisualizer.js") + "\"></script>");
      writer.append("</head>\n<body>");
      writer.append("<table id=\"gridtree-partitur\" class=\"grid-tree partitur_table\">\n");
      writer.append(findAnnotation(input.getResult().getGraph(), input, spans));
      writer.append("</table>\n");
      writer.append("</body></html>");

    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
    }
  }

  private String findAnnotation(AnnotationGraph graph, VisualizerInput input,
    ArrayList<Span> spans)
  {

    List<AnnisNode> nodes = graph.getNodes();
    List<AnnisNode> result = graph.getTokens();
    StringBuilder sb = new StringBuilder();
    String anno = input.getMappings().getProperty(PROPERTY_KEY);

    // cat is the default value and build qualified name
    anno = (anno == null) ? "cat" : anno;
    anno = input.getNamespace() + ":" + anno;

    for (AnnisNode n : nodes)
    {
      if (hasAnno(n, anno))
      {
        Span tmp = new Span(n, result.get(0).getTokenIndex(), result.size(),
          anno);
        spans.add(tmp);
      }
    }

    Collections.sort(spans);

    // print result
    htmlTableRow(sb, result, spans, anno);

    htmlTableRow(sb, result);

    return sb.toString();
  }

  ;

  /**
   * Returns the annotation of a {@link AnnisNode}
   * 
   * @param n
   * @param anno
   * @return null, if the annotation not exists.
   */
  private String getAnnoValue(AnnisNode n, String anno)
  {

    for (Annotation a : n.getNodeAnnotations())
    {
      if (a.getQualifiedName().equals(anno))
      {
        return a.getValue();
      }
    }

    return null;
  }

  /**
   * Returns true when the node is annotated with the string.
   *
   * @param n the node to check
   * @param annotation String to check
   * @return
   */
  private boolean hasAnno(AnnisNode n, String annotation)
  {
    if (n == null)
    {
      return false;
    }

    for (Annotation x : n.getNodeAnnotations())
    {
      if (x.getQualifiedName().equals(annotation))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Build a html-table-row.
   *
   * @param sb the html-code, where the row is embedded
   * @param result List of all results, the list must be sorted by the
   * token-index
   * @param spans Sorted List of Span objects with left and right limit
   * @param anno the anno, which matches to all Span-Objects
   */
  private void htmlTableRow(StringBuilder sb, List<AnnisNode> result,
    ArrayList<Span> spans, String anno)
  {

    int j = 0;
    while (j < spans.size())
    {

      Span tmp = spans.get(j);
      int level = tmp.height;

      // start table line
      sb.append("<tr>\n<th>level: ");
      sb.append(level);
      sb.append("</th>");

      for (int i = 0; i < result.size(); i++)
      {

        if (j < spans.size()) // check if there is a span left
        {
          tmp = spans.get(j);
        }

        // shift the index
        long index = i + result.get(0).getTokenIndex();
        if (tmp.isInIntervall(index) && level == tmp.height)
        {
          tmp.colspan(sb, anno);
          // skip iteration which where covered by colspan
          i += Math.abs(tmp.right - tmp.left);
          j++; // take next span
        }
        else
        {
          sb.append("<td></td>");
        }

      }

      // end table line
      sb.append("</tr>\n");
    }
  }

  /**
   * Build a simple html-row
   *
   * @param sb
   * @param result
   */
  private void htmlTableRow(StringBuilder sb, List<AnnisNode> result)
  {

    sb.append("<tr>\n");
    sb.append("<th> tok </th>");

    for (AnnisNode n : result)
    {
      sb.append("<td>").append(n.getSpannedText()).append("</td>");
    }

    sb.append("</tr>\n");
  }
}
