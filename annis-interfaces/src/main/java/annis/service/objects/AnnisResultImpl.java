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
package annis.service.objects;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisToken;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@Deprecated
public class AnnisResultImpl implements AnnisResult
{

	// this class is sent to the frontend
	

	// the wrapped graph object
	private AnnotationGraph graph;

	// cache paula string
	private String paulaString = null;

	public AnnisResultImpl()
	{
	}

	public AnnisResultImpl(AnnotationGraph graph)
	{
		this.graph = graph;
	}

	public List<AnnisToken> getTokenList()
	{
		List<AnnisToken> result = new ArrayList<AnnisToken>();
		for (AnnisNode node : graph.getTokens())
		{
			AnnisTokenImpl annisToken = new AnnisTokenImpl(node.getId(),
					node.getSpannedText(), node.getLeft(), node.getRight(),
					node.getTokenIndex(), node.getCorpus());
			for (Annotation annotation : node.getNodeAnnotations())
			{
				annisToken.put(annotation.getQualifiedName(),
						annotation.getValue());
			}
			result.add(annisToken);
		}
		return result;
	}

	public long getStartNodeId()
	{
		List<AnnisNode> tokens = graph.getTokens();
		if (!tokens.isEmpty())
		{
			return tokens.get(0).getId();
		} else
		{
			return 0;
		}
	}

	public long getEndNodeId()
	{
		List<AnnisNode> tokens = graph.getTokens();
		if (!tokens.isEmpty())
		{
			return tokens.get(tokens.size() - 1).getId();
		} else
		{
			return 0;
		}
	}

	public Set<String> getAnnotationLevelSet()
	{
		Set<String> result = new HashSet<String>();
		for (AnnisNode node : graph.getNodes())
		{
			if (!node.isToken())
			{
				for (Annotation annotation : node.getNodeAnnotations())
				{
					result.add(annotation.getQualifiedName());
				}
			}
		}
		return result;
	}

	public Set<String> getTokenAnnotationLevelSet()
	{
		Set<String> result = new HashSet<String>();
		for (AnnisNode token : graph.getTokens())
		{
			for (Annotation annotation : token.getNodeAnnotations())
			{
				result.add(annotation.getQualifiedName());
			}
		}
		return result;
	}

	public String getMarkerId(Long nodeId)
	{
		return isMarked(nodeId) ? String.valueOf(nodeId) : null;
	}

	public boolean hasMarker(String markerId)
	{
		return isMarked(Long.parseLong(markerId));
	}

	private boolean isMarked(Long nodeId)
	{
		return graph.getMatchedNodeIds().contains(nodeId);
	}

	public AnnotationGraph getGraph()
	{
		return graph;
	}

	public void setGraph(AnnotationGraph graph)
	{
		this.graph = graph;
	}

	@Override
	public String getDocumentName()
	{
		return graph.getDocumentName();
	}

	@Override
	public String[] getPath()
	{
		return graph.getPath();
	}
}
