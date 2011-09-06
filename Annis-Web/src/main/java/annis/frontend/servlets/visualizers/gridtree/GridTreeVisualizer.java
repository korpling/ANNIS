package annis.frontend.servlets.visualizers.gridtree;

import annis.frontend.servlets.visualizers.VisualizerInput;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;

import annis.frontend.servlets.visualizers.WriterVisualizer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * 
 * @author benjamin
 * 
 */
@PluginImplementation
public class GridTreeVisualizer extends WriterVisualizer
{
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
		int height;
		int visits;
		long offset;
		HashMap<Span, Span> nodes = new HashMap<Span, Span>();

		/**
		 * left and right should be initiate with null, when root is not a
		 * token.
		 * 
		 * @param root
		 * @param r
		 *            must be a sorted List of the result
		 */
		public Span(AnnisNode root, long offset)
		{
			this.root = root;
			this.offset = offset;
		}

		@Override
		/**
		 * this function assumes the spans doesn't have conflicts which, means
		 * that: <br />
		 * sp.height == this.height => [sp.left, sp.right] &cap; [this.left,
		 * sp.right] == &empty;
		 * 
		 * 
		 */
		public int compareTo(Span sp)
		{
			if (this.height < sp.height)
				return 1;
			if (this.height == sp.height)
			{
				if (this.left > sp.right)
				{
					return 1;
				} else
				{
					return -1;
				}
			}
			return -1;
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

		public void colpan(StringBuilder sb, String anno, VisualizerInput input)
		{
			sb.append("<td colspan=\"");
			sb.append(Math.abs(this.right - this.left) + 1);

			// cell-index for hover-effect
			sb.append("\" id=\"intervall:");
			sb.append(this.hashCode());
			sb.append(":");
			sb.append(left + 1 - offset);
			sb.append("-");
			sb.append(right + 1 - offset);

			sb.append("\" class=\"gridtree-result\">");
			sb.append(getAnnoValue(this.root,
					input.getMappings().getProperty("node_key", "cat")));
			sb.append("</td>");
		}

		public boolean isInIntervall(long l)
		{
			if (this.left <= l && l <= this.right)
				return true;
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
    
		try
		{
			writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");

			writer.append("<link href=\""
					+ input.getResourcePath("jquery.tooltip.css")
					+ "\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<link href=\""
					+ input.getResourcePath("partitur.css")
					+ "\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<link href=\""
					+ input.getResourcePath("gridtree.css")
					+ "\" rel=\"stylesheet\" type=\"text/css\" >");

			writer.append("<script type=\"text/javascript\" src=\""
					+ input.getResourcePath("jquery-1.6.2.min.js")
					+ "\"></script>");
			writer.append("<script type=\"text/javascript\" src=\""
					+ input.getResourcePath("jquery.tooltip.min.js")
					+ "\"></script>");

			writer.append("<script type=\"text/javascript\" src=\""
					+ input.getResourcePath("gridtreeVisualizer.js")
					+ "\"></script>");
			writer.append("<body>");
			writer.append("<table id=\"gridtree-partitur\" class=\"grid-tree partitur_table\">\n");
			writer.append(findAnnotation(input.getNamespace(), input
					.getResult().getGraph(), input, spans));
			writer.append("</table>\n");
			writer.append("</body></html>");

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findAnnotation(String anno, AnnotationGraph graph, VisualizerInput input,
    ArrayList<Span> spans)
	{

		List<AnnisNode> nodes = graph.getNodes();
		List<AnnisNode> result = graph.getTokens();
		Set<Span> roots = new HashSet<Span>();

		for (AnnisNode n : nodes)
			if (hasAnno(n, anno))
				roots.add(new Span(n, result.get(0).getTokenIndex()));

		StringBuilder sb = new StringBuilder();

		for (Span n : roots)
		{
			// catch the result
			getTokens(n, roots);
			spans.add(n);
		}

		adaptTreeHeightToMax(roots);
		Collections.sort(spans);

		// print result
		htmlTableRow(sb, result, spans, anno, input);

		htmlTableRow(sb, result);

		return sb.toString();
	};

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
			if (a.getName().equals(anno))
				return a.getValue();
		}

		return null;
	}

	/**
	 * Returns true when the node is annotated with the string. It is not
	 * sensitive to namespaces.
	 * 
	 * @param n
	 *            the node to check
	 * @param annotation
	 *            String to check, without namespaces
	 * @return
	 */
	private boolean hasAnno(AnnisNode n, String annotation)
	{
    
		Set<Annotation> annos = n.getNodeAnnotations();

		for (Annotation x : annos)
			if (x.getNamespace().equals(annotation))
				return true;

		return false;
	}

	/**
	 * Steps from the root recursive through all children nodes to find the
	 * tokens. This function is a straight forward DFS-Algorithm. It also
	 * calculate the max-depth with this functional pseudo-algorithm:<br/>
	 * 
	 * data Tree a = Node [Tree a] | Leaf a<br />
	 * 
	 * maxDepth :: Tree a -> Int <br />
	 * maxDepth (Leaf a) = 0 <br />
	 * maxDepth (Node ls) = maximum (map maxDepth ls) + 1
	 * 
	 * @param n
	 *            is the root
	 * @param roots
	 *            List of root nodes
	 * 
	 */
	public void getTokens(Span n, Set<Span> roots)
	{
		getTokens(n, n.root, roots, 0);
	}

	private void getTokens(Span n, AnnisNode current, Set<Span> roots,
			int height)
	{
		Set<Edge> edges = current.getOutgoingEdges();

		for (Edge e : edges)
		{
      if("edge".equals(e.getName()))
      {
        AnnisNode x = e.getDestination();

        for (Span r : roots)
        {
          if (r.root == x)
          {
            n.nodes.put(r, r);
            r.visits++;
            break;
          }
        }

        if (x.isToken())
        {

          Long tokenIndex = x.getTokenIndex();

          if (n.left == null)
            n.left = tokenIndex;
          else
            n.left = Math.min(n.left, tokenIndex);

          if (n.right == null)
            n.right = tokenIndex;
          else
            n.right = Math.max(n.right, tokenIndex);

          n.height = Math.max(n.height, height);

        }

        // recursive step
        else
        {
          getTokens(n, x, roots, height + 1);
        }
      }
		}
	}

	private void adaptTreeHeightToMax(Set<Span> roots)
	{
		int maxHeight = 0;
		LinkedList<Span> trees = new LinkedList<Span>();

		// find trees
		for (Span s : roots)
		{
			if (maxHeight < s.height)
				maxHeight = s.height;
			if (s.visits == 0)
				trees.add(s);
		}

		// sum offset to trees, which are lower than the highest tree
		for (Span t : trees)
			if (t.height < maxHeight)
				sumHeight(t, maxHeight - t.height);

	}

	private void sumHeight(Span t, int distance)
	{
		t.height += distance;
		for (Span s : t.nodes.values())
		{
			s.height = s.height + distance;
		}
	}

	/**
	 * Build a html-table-row.
	 * 
	 * @param sb
	 *            the html-code, where the row is embedded
	 * @param result
	 *            List of all results, the list must be sorted by the
	 *            token-index
	 * @param spans
	 *            Sorted List of Span objects with left and right limit
	 * @param anno
	 *            the anno, which matches to all Span-Objects
	 */
	private void htmlTableRow(StringBuilder sb, List<AnnisNode> result,
			ArrayList<Span> spans, String anno, VisualizerInput input)
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
					tmp = spans.get(j);

				// shift the index
				long index = i + result.get(0).getTokenIndex();
				if (tmp.isInIntervall(index) && level == tmp.height)
				{
					tmp.colpan(sb, anno, input);
					// skip iteration which where covered by colspan
					i += Math.abs(tmp.right - tmp.left);
					j++; // take next span
				} else
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
			sb.append("<td>" + n.getSpannedText() + "</td>");
		}

		sb.append("</tr>\n");
	}
}
