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
package org.corpus_tools.annis.gui.exporter;

import com.google.common.base.Splitter;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.corpus_tools.annis.gui.util.Helper;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.springframework.stereotype.Component;

@Component
public class GridExporter extends GeneralTextExporter { // NO_UCD (unused code)

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridExporter.class);

    private static class Span {

        private long start;
        private long end;
        private String value;

        public Span(long start, long end, String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public long getEnd() {
            return end;
        }

        public long getStart() {
            return start;
        }

        public String getValue() {
            return value;
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 5344106264419931470L;

    @Override
    public void convertText(SaltProject queryResult, List<String> keys, Map<String, String> args, Writer out,
            int offset, UI ui) throws IOException {

      Map<String, Map<String, SMetaAnnotation>> metadataCache = new HashMap<>();

        boolean showNumbers = true;
        if (args.containsKey("numbers")) {
            String arg = args.get("numbers");
            if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("0") || arg.equalsIgnoreCase("off")) {
                showNumbers = false;
            }
        }
        List<String> metaKeys = new LinkedList<>();
        if (args.containsKey("metakeys")) {
            Iterable<String> it = Splitter.on(",").trimResults().split(args.get("metakeys"));
            for (String s : it) {
                metaKeys.add(s);
            }
        }

        int counter = 0;
        for (SCorpusGraph corpusGraph : queryResult.getCorpusGraphs()) {
            for (SDocument doc : corpusGraph.getDocuments()) {
                SDocumentGraph graph = doc.getDocumentGraph();
                HashMap<String, TreeMap<Long, Span>> annos = new HashMap<>();

                counter++;
                out.append((counter + offset) + ".");
                List<SToken> tokens = graph.getSortedTokenByText();
                Map<SToken, Long> token2index = new HashMap<>();
                {
                    long i = 0;
                    for (SToken t : tokens) {
                        token2index.put(t, i++);
                    }
                }

                for (SNode resolveNode : graph.getNodes()) {

                    List<SToken> coveredTokens = graph.getOverlappedTokens(resolveNode);
                    if (coveredTokens == null || coveredTokens.isEmpty()) {
                      if (resolveNode instanceof SStructuredNode) {
                        log.warn(
                            "Did not find any covered token for node {} in match {}. "
                                + "The node will not be included in the grid export.",
                            resolveNode.getId(), offset + 1);

                      }
                      continue;
                    }
                    coveredTokens = graph.getSortedTokenByText(coveredTokens);

                    for (SAnnotation resolveAnnotation : resolveNode.getAnnotations()) {
                        String k = resolveAnnotation.getName();
                        if (annos.get(k) == null) {
                            annos.put(k, new TreeMap<>());
                        }

                        // create a separate span for every annotation
                        long left_token_idx = token2index.get(coveredTokens.get(0));
                        long right_token_idx = token2index.get(coveredTokens.get(coveredTokens.size() - 1));
                        annos.get(k).put(left_token_idx,
                                new Span(left_token_idx, right_token_idx, resolveAnnotation.getValue().toString()));

                    }
                }

                if (annos.isEmpty()) {
                  log.warn(
                      "No node in the match {} was connected to a token. "
                          + "The match will be empty in the grid exporter",
                      offset + 1);
                }

                for (String k : keys) {

                    if ("tok".equals(k)) {
                        out.append("\t" + k + "\t ");
                        for (SToken annisNode : tokens) {
                            out.append(graph.getText(annisNode) + " ");
                        }
                        out.append("\n");
                    } else {
                        if (annos.get(k) != null) {
                            out.append("\t" + k + "\t ");
                            for (Span s : annos.get(k).values()) {

                                out.append(s.getValue());

                                if (showNumbers) {
                                    long leftIndex = Math.max(1, s.getStart());
                                    long rightIndex = s.getEnd();
                                    out.append("[" + leftIndex + "-" + rightIndex + "]");
                                }
                                out.append(" ");

                            }
                            out.append("\n");
                        }
                    }
                }

                if (!metaKeys.isEmpty()) {
                  String[] path = Helper.getCorpusPath(corpusGraph, doc).toArray(new String[0]);
                    super.appendMetaData(out, metaKeys, path[path.length - 1], path[0], metadataCache, ui);
                }
                out.append("\n\n");
            }
        }
    }

    @Override
    public String getHelpMessage() {
        return "The Grid Exporter can export all annotations of a search result and its "
                + "context. Each annotation layer is represented in a separate line, and the "
                + "tokens covered by each annotation are given as number ranges after each "
                + "annotation in brackets. To suppress token numbers, input numbers=false "
                + "into the parameters box below. To display only a subset of annotations "
                + "in any order use the \"Annotation keys\" text field, input e.g. \"tok,pos,cat\" "
                + "to show tokens and the " + "annotations pos and cat.<br /><br />" + "Parameters: <br/>"
                + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
                + "<code>metakeys=title,documentname</code>) <br />"
                + "<em>numbers</em> - set to \"false\" if the grid event numbers should not be included in the output (e.g. "
                + "<code>numbers=false</code>)";
    }


    @Override
    public boolean isAlignable() {
        return false;
    }
}
