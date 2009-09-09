package annis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AnnotationGraph implements Serializable {

	// this class is sent to the front end
	private static final long serialVersionUID = -1525612317405210436L;

	// graph is defined by list of nodes and tokens
	private List<AnnisNode> nodes;
	private List<Edge> edges;
	
	// annotation graph for nodes with these ids
	private Set<Long> matchedNodeIds;
	
	// fn: token index -> token
	private Map<Long, AnnisNode> tokenByIndex;
	
	public AnnotationGraph() {
		this(new ArrayList<AnnisNode>(), new ArrayList<Edge>());
	}
	
	public AnnotationGraph(List<AnnisNode> nodes, List<Edge> edges) {
		this.nodes = nodes;
		this.edges = edges;
		this.matchedNodeIds = new HashSet<Long>();
		this.tokenByIndex = new HashMap<Long, AnnisNode>();
	}

	@Override
	public String toString() {
		List<Long> ids = new ArrayList<Long>();
		for (AnnisNode node : nodes)
			ids.add(node.getId());
		List<String> _edges = new ArrayList<String>();
		for (Edge edge : edges) {
			Long src = edge.getSource() != null ? edge.getSource().getId() : null;
			long dst = edge.getDestination().getId();
			String edgeType = edge.getEdgeType() != null ? edge.getEdgeType().toString() : null;
			String name = edge.getQualifiedName();
			_edges.add(src + "->" + dst + " " + name + " " + edgeType);
		}
		return "match: " + StringUtils.join(matchedNodeIds, "-") + "; nodes: " + ids + "; edges: " + _edges;
	}
	
	public void addMatchedNodeId(Long id) {
		matchedNodeIds.add(id);
	}
	
	public boolean addNode(AnnisNode node) {
		// save the graph in node
		node.setGraph(this);
		
		// save tokens
		if (node.isToken())
			tokenByIndex.put(node.getTokenIndex(), node);
		
		// add node to graph	
		return nodes.add(node);
	}
	
	public boolean addEdge(Edge edge) {
		return edges.add(edge);
	}
	
	public AnnisNode getToken(long tokenIndex) {
		return tokenByIndex.get(tokenIndex);
	}

	public List<AnnisNode> getTokens() {
		List<AnnisNode> tokens = new ArrayList<AnnisNode>();
		for (AnnisNode node : nodes) {
			if (node.isToken())
				tokens.add(node);
		}
		Collections.sort(tokens, new Comparator<AnnisNode>() {

			public int compare(AnnisNode o1, AnnisNode o2) {
				return o1.getTokenIndex().compareTo(o2.getTokenIndex());
			}
			
		});
		return tokens;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AnnotationGraph))
			return false;

		AnnotationGraph other = (AnnotationGraph) obj;
		
		return new EqualsBuilder()
			.append(this.nodes, other.nodes)
			.append(this.edges, other.edges)
			.append(this.matchedNodeIds, other.matchedNodeIds)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(nodes).append(edges).append(matchedNodeIds).toHashCode();
	}

	///// Getter / Setter
	
	public List<AnnisNode> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}
	
	public Set<Long> getMatchedNodeIds() {
		return matchedNodeIds;
	}

}
