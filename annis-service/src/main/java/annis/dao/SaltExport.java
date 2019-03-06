/*
 * Copyright 2017 Thomas Krause.
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
package annis.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.corpus_tools.graphannis.model.Component;
import org.corpus_tools.graphannis.model.ComponentType;
import org.corpus_tools.graphannis.model.Edge;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.graphannis.model.Node;
import org.corpus_tools.graphannis.model.QName;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

/**
 * Allows to extract a Salt-Graph from a database subgraph.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 */
public class SaltExport {

	private final Graph orig;
	private final SDocumentGraph docGraph;
	private final BiMap<Integer, SNode> nodesByID;
	private final Map<Integer, Integer> node2timelinePOT;

	protected SaltExport(Graph orig) {
		this.orig = orig;

		this.docGraph = SaltFactory.createSDocumentGraph();
		this.nodesByID = HashBiMap.create();
		this.node2timelinePOT = new HashMap<>();
	}

	private static void mapLabels(SAnnotationContainer n, Map<QName, String> labels, boolean isMeta) {
		for (Map.Entry<QName, String> e : labels.entrySet()) {
			if ("annis".equals(e.getKey().getNs())) {
				n.createFeature(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			} else if (isMeta) {
				n.createMetaAnnotation(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			} else {
				n.createAnnotation(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			}
		}

	}

	private boolean hasDominanceEdge(Node node) {

		List<Edge> outEdges = orig.getOutgoingEdges(node, ComponentType.Dominance);
		return !outEdges.isEmpty();
	}

	private boolean hasCoverageEdge(Node node) {
		List<Edge> outEdges = orig.getOutgoingEdges(node, ComponentType.Coverage);
		return !outEdges.isEmpty();
	}

	private SNode mapNode(Node node) {
		SNode newNode;

		// get all annotations for the node into a map, also create the node itself
		Map<QName, String> labels = node.getLabels();

		if (labels.containsKey(Graph.TOK) && !hasCoverageEdge(node)) {
			newNode = SaltFactory.createSToken();
		} else if (hasDominanceEdge(node)) {
			newNode = SaltFactory.createSStructure();
		} else {
			newNode = SaltFactory.createSSpan();
		}

		String nodeName = node.getName();
		if (!nodeName.startsWith("salt:/")) {
			nodeName = "salt:/" + nodeName;
		}
		newNode.setId(nodeName);
		// get the name from the ID
		newNode.setName(newNode.getPath().fragment());

		mapLabels(newNode, labels, false);

		return newNode;
	}

	private void mapAndAddEdge(Edge origEdge) {
		SNode source = nodesByID.get(origEdge.getSourceID());
		SNode target = nodesByID.get(origEdge.getTargetID());

		String edgeType = origEdge.getComponent().getName();
		if (source != null && target != null && source != target) {

			SRelation<?, ?> rel = null;
			switch (origEdge.getComponent().getType()) {
			case Dominance:
				if (edgeType.isEmpty()) {
					// We don't include edges that have no type if there is an edge
					// between the same nodes which has a type.
					List<Edge> domOutEdges = orig.getOutgoingEdges(origEdge.getSource(), ComponentType.Dominance);
					for (Edge outEdge : domOutEdges) {
						if (outEdge.getTargetID() == origEdge.getTargetID()
								&& !outEdge.getComponent().getName().isEmpty()) {
							// exclude this relation
							return;
						}
					}
				} // end mirror check
				rel = docGraph.createRelation(source, target, SALT_TYPE.SDOMINANCE_RELATION, null);

				break;
			case Pointing:
				rel = docGraph.createRelation(source, target, SALT_TYPE.SPOINTING_RELATION, null);
				break;
			case Ordering:
				rel = docGraph.createRelation(source, target, SALT_TYPE.SORDER_RELATION, null);
				break;
			case Coverage:
				// only add coverage edges in salt to spans, not structures
				if (source instanceof SSpan && target instanceof SToken) {
					rel = docGraph.createRelation(source, target, SALT_TYPE.SSPANNING_RELATION, null);
				}
				break;
			}

			if (rel != null) {
				rel.setType(edgeType);

				// map edge labels
				mapLabels(rel, origEdge.getLabels(), false);

				String layerName = origEdge.getComponent().getLayer();
				if (layerName != null && !layerName.isEmpty()) {
					List<SLayer> layer = docGraph.getLayerByName(layerName);
					if (layer == null || layer.isEmpty()) {
						SLayer newLayer = SaltFactory.createSLayer();
						newLayer.setName(layerName);
						docGraph.addLayer(newLayer);
						layer = Arrays.asList(newLayer);
					}
					layer.get(0).addRelation(rel);
				}
			}
		}
	}

	private void addNodeLayers() {
		List<SNode> nodeList = new LinkedList<>(docGraph.getNodes());
		for (SNode n : nodeList) {
			SFeature featLayer = n.getFeature("annis", "layer");
			if (featLayer != null) {
				String layerName = featLayer.getValue_STEXT();
				List<SLayer> layer = docGraph.getLayerByName(layerName);
				if (layer == null || layer.isEmpty()) {
					SLayer newLayer = SaltFactory.createSLayer();
					newLayer.setName(layerName);
					docGraph.addLayer(newLayer);
					layer = Arrays.asList(newLayer);
				}
				layer.get(0).addNode(n);
			}
		}
	}

	private void recreateText(final String name, List<SNode> rootNodes) {
		final StringBuilder text = new StringBuilder();
		final STextualDS ds = docGraph.createTextualDS("");

		ds.setName(name);

		Map<SToken, Range<Integer>> token2Range = new HashMap<>();

		// traverse the token chain using the order relations
		docGraph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
				new GraphTraverseHandler() {
					@Override
					public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {
						if (fromNode != null) {
							text.append(" ");
						}

						SFeature featTok = currNode.getFeature("annis::tok");
						if (featTok != null && currNode instanceof SToken) {
							int idxStart = text.length();
							text.append(featTok.getValue_STEXT());
							token2Range.put((SToken) currNode, Range.closed(idxStart, text.length()));
						}
					}

					@Override
					public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
							SRelation<SNode, SNode> relation, SNode fromNode, long order) {
					}

					@SuppressWarnings("rawtypes")
					@Override
					public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SRelation relation, SNode currNode, long order) {
						if (relation == null) {
							// TODO: check if this is ever true
							return true;
						} else if (relation instanceof SOrderRelation && Objects.equal(name, relation.getType())) {
							return true;
						} else {
							return false;
						}
					}
				});

		// update the actual text
		ds.setText(text.toString());

		// add all relations
		token2Range.forEach((t, r) -> {
			STextualRelation rel = SaltFactory.createSTextualRelation();
			rel.setSource(t);
			rel.setTarget(ds);
			rel.setStart(r.lowerEndpoint());
			rel.setEnd(r.upperEndpoint());
			docGraph.addRelation(rel);
		});

		if (docGraph.getTimeline() != null) {

			// create the relations to the timeline for the tokens of this text by getting
			// the original node IDs of the coverage edges and their mapping to a point of
			// time (POT)
			for (SToken tok : token2Range.keySet()) {
				Integer tokID = nodesByID.inverse().get(tok);
				if (tokID != null) {
					Integer tokPOT = this.node2timelinePOT.get(tokID);
					if (tokPOT != null) {
						// directly map the relation of the token to its POT
						STimelineRelation rel = SaltFactory.createSTimelineRelation();
						rel.setSource(tok);
						rel.setTarget(docGraph.getTimeline());
						rel.setStart(tokPOT);
						rel.setEnd(tokPOT);
						docGraph.addRelation(rel);
					} else {
						// find the coverage edges from this node to a token which has a POT
						Component covCompoment = new Component(ComponentType.Coverage, "annis", "");
						List<Edge> edges = orig.getOutgoingEdges(orig.getNodeForID(tokID), covCompoment);
						for (Edge e : edges) {
							Integer pot = this.node2timelinePOT.get(e.getTargetID());
							if (pot != null) {
								STimelineRelation rel = SaltFactory.createSTimelineRelation();
								rel.setSource(tok);
								rel.setTarget(docGraph.getTimeline());
								rel.setStart(pot);
								rel.setEnd(pot);
								docGraph.addRelation(rel);
							}

						}
					}
				}
			}

		}
	}

	private void addTextToSegmentation(final String name, List<SNode> rootNodes) {

		// traverse the token chain using the order relations
		docGraph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
				new GraphTraverseHandler() {
					@Override
					public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {

						SFeature featTok = currNode.getFeature("annis::tok");
						if (featTok != null && currNode instanceof SSpan) {
							currNode.createAnnotation(null, name, featTok.getValue().toString());
						}
					}

					@Override
					public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
							SRelation<SNode, SNode> relation, SNode fromNode, long order) {
					}

					@Override
					public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SRelation relation, SNode currNode, long order) {
						if (relation == null) {
							// TODO: check if this is ever true
							return true;
						} else if (relation instanceof SOrderRelation && Objects.equal(name, relation.getType())) {
							return true;
						} else {
							return false;
						}
					}
				});
	}

	public static SDocumentGraph map(Graph orig) {
		if (orig == null) {
			return null;
		}
		SaltExport export = new SaltExport(orig);

		export.mapDocGraph();
		return export.docGraph;
	}

	private void mapDocGraph() {

		// create all new nodes
		List<Edge> edges = new LinkedList<>();
		for (Node node : orig.getNodesByType("node")) {
			SNode n = mapNode(node);
			nodesByID.put(node.getId(), n);
			edges.addAll(orig.getOutgoingEdges(node, ComponentType.Dominance));
			edges.addAll(orig.getOutgoingEdges(node, ComponentType.Coverage));
			edges.addAll(orig.getOutgoingEdges(node, ComponentType.Pointing));
			edges.addAll(orig.getOutgoingEdges(node, ComponentType.Ordering));
		}

		// add nodes to the graph
		nodesByID.values().stream().forEach(n -> docGraph.addNode(n));

		// create and add all edges
		for (Edge e : edges) {
			mapAndAddEdge(e);
		}

		// find all chains of SOrderRelations and reconstruct the texts belonging to
		// them
		Multimap<String, SNode> orderRoots = docGraph.getRootsByRelationType(SALT_TYPE.SORDER_RELATION);
		orderRoots.keySet().forEach((name) -> {
			ArrayList<SNode> roots = new ArrayList<>(orderRoots.get(name));
			if (SaltUtil.SALT_NULL_VALUE.equals(name)) {
				name = null;
			}
			if (name == null || "".equals(name)) {
				// only re-create text if this is the default (possible virtual) tokenization
				recreateText(name, roots);
			} else {
				// add the text as label to the spans
				addTextToSegmentation(name, roots);
			}
		});

		addNodeLayers();
	}

	private static SCorpus addCorpusAndParents(SCorpusGraph cg, Node node, Map<Node, Node> parentOfNode,
			Map<Integer, SCorpus> id2corpus) {

		if (id2corpus.containsKey(node.getId())) {
			return id2corpus.get(node.getId());
		}

		// create parents first
		Node parentNode = parentOfNode.get(node);
		SCorpus parent = null;
		if (parentNode != null) {
			parent = addCorpusAndParents(cg, parentNode, parentOfNode, id2corpus);
		}

		String corpusName = node.getName();
		List<String> corpusNameSplitted = Splitter.on('/').trimResults().splitToList(corpusName);
		// use last part of the path as name
		SCorpus newCorpus = cg.createCorpus(parent, corpusNameSplitted.get(corpusNameSplitted.size() - 1));
		mapLabels(newCorpus, node.getLabels(), true);

		id2corpus.put(node.getId(), newCorpus);

		return newCorpus;

	}

	public static SCorpusGraph mapCorpusGraph(Graph orig) {
		if (orig == null) {
			return null;
		}
		SCorpusGraph cg = SaltFactory.createSCorpusGraph();

		Map<Node, Node> parentOfNode = new LinkedHashMap<>();

		// iterate over all nodes and get their outgoing edges
		for (Node n : orig.getNodesByType("corpus")) {
			List<Edge> outEdges = orig.getOutgoingEdges(n, ComponentType.PartOf);
			for (Edge edge : outEdges) {
				parentOfNode.put(edge.getSource(), edge.getTarget());
			}
		}

		Map<Integer, SCorpus> id2corpus = new HashMap<>();
		// add all non-documents first
		for (Node node : parentOfNode.values()) {
			addCorpusAndParents(cg, node, parentOfNode, id2corpus);
		}

		// add all documents next
		for (Map.Entry<Node, Node> edge : parentOfNode.entrySet()) {
			int childID = edge.getKey().getId();
			int parentID = edge.getValue().getId();
			if (!id2corpus.containsKey(childID)) {
				Map<QName, String> labels = edge.getKey().getLabels();
				String docName = labels.getOrDefault(new QName("annis", "doc"), "document");
				SCorpus parent = id2corpus.get(parentID);
				SDocument doc = cg.createDocument(parent, docName);

				mapLabels(doc, labels, true);

			}
		}

		return cg;
	}
}
