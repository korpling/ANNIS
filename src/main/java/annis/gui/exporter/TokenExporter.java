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
package annis.gui.exporter;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

import annis.libgui.Helper;
import com.google.common.base.Splitter;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.springframework.stereotype.Component;

@Component
public class TokenExporter extends GeneralTextExporter { // NO_UCD (unused code)

    /**
     * 
     */
    private static final long serialVersionUID = 218197959064424601L;

    @Override
    public void convertText(SaltProject queryResult, List<String> keys, Map<String, String> args, Writer out,
            int offset, UI ui) throws IOException {

      Map<String, Map<String, SMetaAnnotation>> metadataCache = new HashMap<>();

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

                counter++;
                out.append((counter + offset) + ". ");
                List<SToken> tok = graph.getSortedTokenByText();

                for (SToken annisNode : tok) {
                    SFeature featMatched = annisNode.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
                    Long match = featMatched == null ? null : featMatched.getValue_SNUMERIC();
                    if (match != null) {
                        out.append("[");
                        out.append(graph.getText(annisNode));
                        out.append("]");
                    } else {
                        out.append(graph.getText(annisNode));
                    }

                    for (SAnnotation annotation : annisNode.getAnnotations()) {
                        out.append("/" + annotation.getValue());
                    }

                    out.append(" ");

                }
                out.append("\n");

                if (!metaKeys.isEmpty()) {
                  String[] path = Helper.getCorpusPath(corpusGraph, doc).toArray(new String[0]);
                    super.appendMetaData(out, metaKeys, path[path.length - 1], path[0], metadataCache, ui);
                }
                out.append("\n");
            }
        }
    }

    @Override
    public String getHelpMessage() {
        return "The Token Exporter exports the token covered by the matched nodes of every search result and "
                + "its context, one line per result. "
                + "Beside the text of the token it also contains all token annotations separated by \"/\"." + "<p>"
                + "<strong>This exporter does not work well with dialog data "
                + "(corpora that have more than one primary text). " + "Use the GridExporter instead.</strong>"
                + "</p>";
    }

    @Override
    public boolean isAlignable() {
        return false;
    }

}
