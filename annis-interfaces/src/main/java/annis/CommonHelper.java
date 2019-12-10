/*
 * Copyright 2011 SFB 632.
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
package annis;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.google.common.base.Joiner;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.model.AnnisConstants;
import annis.service.objects.Match;

/**
 * Utilities class for non-gui operations.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class CommonHelper {

    private final static Logger log = LoggerFactory.getLogger(CommonHelper.class);

    /**
     * Detects arabic characters in a string.
     *
     * <p>
     * Every character is checked, if its bit representation lies between:
     * <code>[1425, 1785] | [64286, 65019] | [65136, 65276]</code>
     *
     * </p>
     *
     * @param str
     *                The string to be checked.
     * @return returns true, if arabic characters are detected.
     */
    public static boolean containsRTLText(String str) {
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char cc = str.charAt(i);
                // hebrew extended and basic, arabic basic and extendend
                if (cc >= 1425 && cc <= 1785) {
                    return true;
                }
                // alphabetic presentations forms (hebrwew) to arabic presentation forms A
                else if (cc >= 64286 && cc <= 65019) {
                    return true;
                }
                // arabic presentation forms B
                else if (cc >= 65136 && cc <= 65276) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates a {@link SOrderRelation} node chain of a {@link SDocumentGraph}.
     *
     * <p>
     * If no segmentation name is set, a list of sorted {@link SToken} will be
     * returned.
     * <p>
     *
     * @param segName
     *                    The segmentation name, for which the chain is computed.
     * @param graph
     *                    The salt document graph, which is traversed for the
     *                    segmentation.
     *
     * @return Returns a List of {@link SNode}, which is sorted by the
     *         {@link SOrderRelation}.
     */
    public static List<SNode> getSortedSegmentationNodes(String segName, SDocumentGraph graph) {
        List<SNode> token = new ArrayList<SNode>();

        if (segName == null) {
            // if no segmentation is given just return the sorted token list
            List<SToken> unsortedToken = graph.getSortedTokenByText();
            if (unsortedToken != null) {
                token.addAll(unsortedToken);
            }
        } else {
            // get the very first node of the order relation chain
            Set<SNode> startNodes = new LinkedHashSet<SNode>();
            if (graph != null) {
                List<SNode> orderRoots = graph.getRootsByRelation(SALT_TYPE.SORDER_RELATION);
                if (orderRoots != null) {
                    // collect the start nodes of a segmentation chain of length 1
                    for (SNode n : orderRoots) {
                        for (SRelation<?, ?> rel : n.getOutRelations()) {
                            if (rel instanceof SOrderRelation) {
                                // the type is the name of the relation
                                if (segName.equals(rel.getType())) {
                                    startNodes.add(n);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            Set<String> alreadyAdded = new HashSet<String>();

            // add all nodes on the order relation chain beginning from the start node
            for (SNode s : startNodes) {
                SNode current = s;
                while (current != null) {
                    token.add(current);
                    List<SRelation<SNode, SNode>> out = graph.getOutRelations(current.getId());
                    current = null;
                    if (out != null) {
                        for (SRelation<? extends SNode, ? extends SNode> e : out) {
                            if (e instanceof SOrderRelation) {
                                current = ((SOrderRelation) e).getTarget();
                                if (alreadyAdded.contains(current.getId())) {
                                    // abort if cycle detected
                                    current = null;
                                } else {
                                    alreadyAdded.add(current.getId());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        return token;
    }

    public static Set<String> getTokenAnnotationLevelSet(SDocumentGraph graph) {
        Set<String> result = new TreeSet<String>();

        if (graph != null) {
            for (SToken n : graph.getTokens()) {
                for (SAnnotation anno : n.getAnnotations()) {
                    result.add(anno.getQName());
                }
            }
        }

        return result;
    }

    public static Set<String> getTokenAnnotationLevelSet(SaltProject p) {
        Set<String> result = new TreeSet<String>();

        for (SCorpusGraph corpusGraphs : p.getCorpusGraphs()) {
            for (SDocument doc : corpusGraphs.getDocuments()) {
                SDocumentGraph g = doc.getDocumentGraph();
                result.addAll(getTokenAnnotationLevelSet(g));
            }
        }

        return result;
    }

    /**
     * Gets the spannend/covered text for a token. This will get all
     * {@link STextualRelation} edges for a {@link SToken} from the
     * {@link SDocumentGraph} and calculates the appropiate substring from the
     * {@link STextualDS}.
     *
     * @param tok
     *                The {@link SToken} which is overlapping the text sequence.
     * @return An empty {@link String} object, if there is no
     *         {@link STextualRelation}
     */
    public static String getSpannedText(SToken tok) {
        SGraph graph = tok.getGraph();

        List<SRelation<SNode, SNode>> edges = graph.getOutRelations(tok.getId());
        for (SRelation<? extends SNode, ? extends SNode> e : edges) {
            if (e instanceof STextualRelation) {
                STextualRelation textRel = (STextualRelation) e;
                return textRel.getTarget().getText().substring(textRel.getStart(), textRel.getEnd());
            }
        }
        return "";
    }

    /**
     * Checks a {@link SNode} if it is member of a specific {@link SLayer}.
     *
     * @param layerName
     *                      Specifies the layername to check.
     * @param node
     *                      Specifies the node to check.
     * @return true - it is true when the name of layername corresponds to the name
     *         of any label of the SNode.
     */
    public static boolean checkSLayer(String layerName, SNode node) {
        // robustness
        if (layerName == null || node == null) {
            return false;
        }

        Set<SLayer> sLayers = node.getLayers();
        if (sLayers != null) {
            for (SLayer l : sLayers) {
                Collection<Label> labels = l.getLabels();
                if (labels != null) {
                    for (Label label : labels) {
                        if (layerName.equals(label.getValue())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static List<String> getCorpusPath(SCorpusGraph corpusGraph, SDocument doc) {
        final List<String> result = new LinkedList<String>();

        result.add(doc.getName());
        SCorpus c = corpusGraph.getCorpus(doc);
        List<SNode> cAsList = new ArrayList<>();
        cAsList.add(c);
        corpusGraph.traverse(cAsList, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, "getRootCorpora",
                new GraphTraverseHandler() {
                    @Override
                    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
                            SRelation edge, SNode fromNode, long order) {
                        result.add(currNode.getName());
                    }

                    @Override
                    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
                            SRelation edge, SNode fromNode, long order) {
                    }

                    @Override
                    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                            SRelation edge, SNode currNode, long order) {
                        return true;
                    }
                });
        return result;
    }

    public static List<String> getCorpusPath(URI uri) {
        String rawPath = StringUtils.strip(uri.getRawPath(), "/ \t");

        // split on raw path (so "/" in corpus names are still encoded)
        String[] path = rawPath.split("/");

        // decode every single part by itself
        ArrayList<String> result = new ArrayList<>(path.length);
        for (int i = 0; i < path.length; i++) {
            try {
                result.add(URLDecoder.decode(path[i], "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                log.error(null, ex);
                // fallback
                result.add(path[i]);
            }
        }

        return result;
    }

    /**
     * Finds the {@link STextualDS} for a given node. The node must dominate a token
     * of this text.
     *
     * @param node
     *                  Salt node to find the textual data source for
     * @param graph
     *                  document graph
     * @return textual datasource or null if not connected to one
     */
    public static STextualDS getTextualDSForNode(SNode node, SDocumentGraph graph) {
        if (node != null) {
            List<DataSourceSequence> dataSources = graph.getOverlappedDataSourceSequence(node,
                    SALT_TYPE.STEXT_OVERLAPPING_RELATION);
            if (dataSources != null) {
                for (DataSourceSequence seq : dataSources) {
                    if (seq.getDataSource() instanceof STextualDS) {
                        return (STextualDS) seq.getDataSource();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a file name that is safe to use and does not have any invalid
     * characters.
     *
     * @param orig
     *                 original file name
     * @return encoded file name
     */
    public static String getSafeFileName(String orig) {
        if (orig != null) {
            return orig.replaceAll("[^0-9A-Za-z-]", "_");
        } else {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Gets all names of a corpus from a salt project.
     *
     * @param p
     *              Salt project
     * @return returns an empty list if project is empty or null.
     */
    public static Set<String> getToplevelCorpusNames(SaltProject p) {
        Set<String> names = new HashSet<>();

        if (p != null && p.getCorpusGraphs() != null) {
            for (SCorpusGraph g : p.getCorpusGraphs()) {
                if (g.getRoots() != null) {
                    for (SNode c : g.getRoots()) {
                        names.add(c.getName());
                    }
                }
            }
        }

        return names;
    }

    /**
     * Takes a map of salt node IDs to a value and return a new map that uses the
     * SNodes as keys instead of the IDs.
     *
     * @param map
     *                  Map of node IDs
     * @param graph
     *                  document graph
     * @return map with SNodes as key
     */
    public static <V> Map<SNode, V> createSNodeMapFromIDs(Map<String, V> map, SDocumentGraph graph) {
        HashMap<SNode, V> result = new LinkedHashMap<>();

        if (map != null && graph != null) {
            for (Map.Entry<String, V> e : map.entrySet()) {
                SNode n = graph.getNode(e.getKey());
                if (n != null) {
                    result.put(n, e.getValue());
                }
            }
        }

        return result;
    }

    public static void addMatchToDocumentGraph(Match match, SDocument document) {
        List<String> allUrisAsString = new LinkedList<>();
        long i = 1;
        for (URI u : match.getSaltIDs()) {
            allUrisAsString.add(u.toASCIIString().replace(",", "%2C"));
            SNode matchedNode = document.getDocumentGraph().getNode(u.toASCIIString());
            // set the feature for this specific node
            if (matchedNode != null) {
                SFeature existing = matchedNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
                if (existing == null) {
                    SFeature featMatchedNode = SaltFactory.createSFeature();
                    featMatchedNode.setNamespace(AnnisConstants.ANNIS_NS);
                    featMatchedNode.setName(AnnisConstants.FEAT_MATCHEDNODE);
                    featMatchedNode.setValue(i);
                    matchedNode.addFeature(featMatchedNode);
                }
            }
            i++;
        }
        SFeature existingFeatIDs = document.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS);
        if (existingFeatIDs == null) {
            SFeature featIDs = SaltFactory.createSFeature();
            featIDs.setNamespace(AnnisConstants.ANNIS_NS);
            featIDs.setName(AnnisConstants.FEAT_MATCHEDIDS);
            featIDs.setValue(Joiner.on(",").join(allUrisAsString));
            document.addFeature(featIDs);
        }
        
        SFeature existingFeatAnnos = document.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDANNOS);
        if(existingFeatAnnos == null) {
            SFeature featAnnos = SaltFactory.createSFeature();
            featAnnos.setNamespace(AnnisConstants.ANNIS_NS);
            featAnnos.setName(AnnisConstants.FEAT_MATCHEDANNOS);
            featAnnos.setValue(Joiner.on(",").join(match.getAnnos()));
             document.addFeature(featAnnos);
        }
    }

    /**
     * Parses the fragment.
     *
     * Fragments have the form key1=value&key2=test ...
     *
     * @param fragment
     *                     fragment to parse
     * @return a map with the keys and values of the fragment
     */
    public static LinkedHashMap<String, String> parseFragment(String fragment) {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        fragment = StringUtils.removeStart(fragment, "!");

        String[] split = StringUtils.split(fragment, "&");
        if(split != null) {
            for (String s : split) {
                String[] parts = s.split("=", 2);
                String name = parts[0].trim();
                String value = "";
                if (parts.length == 2) {
                    try {
                        // every name that starts with "_" is base64 encoded
                        if (name.startsWith("_")) {
                            value = new String(Base64.decodeBase64(parts[1]), "UTF-8");
                        } else {
                            value = URLDecoder.decode(parts[1], "UTF-8");
                        }
                    } catch (UnsupportedEncodingException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
                name = StringUtils.removeStart(name, "_");
    
                result.put(name, value);
            }
        }
        return result;
    }

    public static String encodeBase64URL(String val) {
        try {
            return Base64.encodeBase64URLSafeString(val.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            log.error("Java Virtual Maschine can't handle UTF-8: I'm utterly confused", ex);
        }
        return "";
    }
}
