/*
 * Copyright 2013 SFB 632.
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
package annis.service;

import annis.model.Annotation;
import annis.service.objects.AnnisBinaryMetaData;
import java.util.List;

/**
 * Interface defining the REST API calls that ANNIS provides for getting the
 * meta data.
 *
 * <p>
 * All paths for this part of the service start with
 * <pre>annis/meta/</pre>
 * </p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface MetadataService
{

  /**
   * Fetches the meta data for a top level corpus.
   *
   * <p>If the <b>closure</b> path is called, the metadata of all subcorpora and
   * documents are also fetched. If the path closure path is selected, all
   * annotations from all subcorpora and documents are fetched.</p>
   *
   *
   * <h3>Path(s)</h3>
   *
   * <ol>
   * <li>GET annis/meta/corpus/<b>{top}</b></li>
   * <li>GET annis/meta/corpus/<b>{top}</b>/closure</li>
   * </ol>
   *
   * <h3>MIME</h3>
   *
   * produces:
   * <code>application/xml</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the annotations are
   * retrieved.
   * @param closure if true, all annotations of all subcorpora or documents
   * contained by the toplevel corpus are also fetched.
   * @return The XML representation of a list wich contains {@link Annotation}
   * objects.
   */
  public List<Annotation> getMetadata(String topLevelCorpus, boolean closure);

  /**
   * Fetches the meta data for a top level corpus.
   *
   * <p>If the <strong>path</strong> path is selected, all annotations from the
   * corpora and subcorpora of the name path are included.</p>
   *
   * <h3>Path(s)</h3>
   *
   * <ol>
   * <li>GET annis/meta/doc/<b>{top}</b></li>
   * <li>GET annis/meta/doc/<b>{top}</b>/<b>{doc}</b></li>
   * <li>GET annis/meta/doc/<b>{top}</b>/<b>{doc}</b>/path</li>
   * </ol>
   *
   * <h3>MIME</h3>
   *
   * accept:
   * <code>application/xml</code>
   * <code>application/json</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the annotations are
   * retrieved.
   * @param docname Determines the document name, for which the annotations are
   * fetched. If null, all annotations of all documents are fetched.
   * @param path if true, the annotations of all corpora and documents contained
   * in the path are also included
   * @return The xml representation of a list wich contains {@link Annotation}
   * objects.
   */
  public List<Annotation> getMetadataDoc(String topLevelCorpus, String docname,
    boolean path);

  /**
   * Fetches all document names within a top level corpus.
   *
   * <h3>Path</h3>
   *
   * <p>GET annis/meta/docnames/<b>{top}</b></p> *
   *
   * <h3>MIME</h3>
   *
   * accept:
   * <code>application/xml</code>
   * <code>application/json</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the document names
   * are retrieved.
   * @return The xml representation of a list wich contains {@link Annotation}
   * objects.
   */
  public List<Annotation> getDocNames(String topLevelCorpus);

  /**
   * Get the metadata of all ANNIS binary objects for a specific document.
   *
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/meta/binary/<b>{top}</b>/<b>{document}</b></li>
   * </ol>
   *
   * <h3>MIME</h3>
   * <div>
   * Accepts
   * <code>application/xml</code>
   * </div>
   *
   * @param top The toplevel corpus name.
   * @param document The name of the document that has the file. If you want the
   * files for the toplevel corpus itself, use the name of the toplevel corpus
   * as document name.
   * @return A XML representation containing a list of all matching
   * {@link annis.service.objects.AnnisBinaryMetaData} objects.
   */
  public List<AnnisBinaryMetaData> binaryMeta(String toplevelCorpusName,
    String documentName);
}
