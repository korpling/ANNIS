/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AnnotationGraph implements Serializable
{

	// this class is sent to the front end
	

	// metadata for searchresult
	private String documentName;
	private String[] path;

	// graph is defined by list of nodes and tokens
	private List<AnnisNode> nodes;
	private List<Edge> edges;

	// annotation graph for nodes with these ids
	private Set<Long> matchedNodeIds;

	// fn: token index -> token
	private Map<Long, AnnisNode> tokenByIndex;

	public AnnotationGraph()
	{
		this(new ArrayList<AnnisNode>(), new ArrayList<Edge>());
	}

	public AnnotationGraph(List<AnnisNode> nodes, List<Edge> edges)
	{
		this.nodes = nodes;
		this.edges = edges;
		this.matchedNodeIds = new HashSet<Long>();
		this.tokenByIndex = new HashMap<Long, AnnisNode>();
	}

	@Override
	public String toString()
	{
		List<Long> ids = new ArrayList<Long>();
		for (AnnisNode node : nodes)
			ids.add(node.getId());
		List<String> _edges = new ArrayList<String>();
		for (Edge edge : edges)
		{
			Long src = edge.getSource() != null ? edge.getSource().getId()
					: null;
			long dst = edge.getDestination().getId();
			String edgeType = edge.getEdgeType() != null ? edge.getEdgeType()
					.toString() : null;
			String name = edge.getQualifiedName();
			_edges.add(src + "->" + dst + " " + name + " " + edgeType);
		}
		return "match: " + StringUtils.join(matchedNodeIds, "-") + "; nodes: "
				+ ids + "; edges: " + _edges;
	}

	public void addMatchedNodeId(Long id)
	{
		matchedNodeIds.add(id);
	}

	public boolean addNode(AnnisNode node)
	{
		// save the graph in node
		node.setGraph(this);

		// save tokens
		if (node.isToken())
			tokenByIndex.put(node.getTokenIndex(), node);

		// add node to graph
		return nodes.add(node);
	}

	public boolean addEdge(Edge relation)
	{
		return edges.add(relation);
	}

	public AnnisNode getToken(long tokenIndex)
	{
		return tokenByIndex.get(tokenIndex);
	}

	public List<AnnisNode> getTokens()
	{
		List<AnnisNode> tokens = new ArrayList<AnnisNode>();
		for (AnnisNode node : nodes)
		{
			if (node.isToken())
				tokens.add(node);
		}
		Collections.sort(tokens, new NodeComparator());
		return tokens;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof AnnotationGraph))
			return false;

		AnnotationGraph other = (AnnotationGraph) obj;

		return new EqualsBuilder().append(this.nodes, other.nodes)
				.append(this.edges, other.edges)
				.append(this.matchedNodeIds, other.matchedNodeIds).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(nodes).append(edges)
				.append(matchedNodeIds).toHashCode();
	}

	// /// Getter / Setter

	public List<AnnisNode> getNodes()
	{
		return nodes;
	}

	public List<Edge> getEdges()
	{
		return edges;
	}

	public Set<Long> getMatchedNodeIds()
	{
		return matchedNodeIds;
	}

	public String getDocumentName()
	{
		return documentName;
	}

	public void setDocumentName(String documentName)
	{
		this.documentName = documentName;
	}

	public String[] getPath()
	{
		return Arrays.copyOf(path, path.length);
	}

	public void setPath(String[] path)
	{
		this.path = Arrays.copyOf(path, path.length);
	}

  private static class NodeComparator implements Comparator<AnnisNode>, Serializable
  {

    public NodeComparator()
    {
    }

    public int compare(AnnisNode o1, AnnisNode o2)
    {
      return o1.getTokenIndex().compareTo(o2.getTokenIndex());
    }
  }

}
