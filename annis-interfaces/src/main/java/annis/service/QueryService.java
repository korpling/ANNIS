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

import annis.service.objects.MatchGroup;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.corpus_tools.salt.common.SaltProject;

/**
 * Interface defining the REST API calls that ANNIS provides for querying the
 * data.
 *
 * <div>
 * All paths for this part of the service start with <pre>annis/query/</pre>
 * </div>
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface QueryService
{
  
  /**
   * Count matches of an AQL query.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/query/search/count</li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * produces:
   * <code>application/xml</code>:<br />
   * {@code
   * <matchAndDocumentCount>
   *   <!-- the number of documents that contain matches -->
   *   <documentCount>2</documentCount>
   *   <!-- total number of matches -->
   *   <matchCount>399</matchCount>
   * </matchAndDocumentCount>
   * }
   * 
   * @param q The AQL query
   * @param corpora A comma separated list of corpus names
   * @return A XML represenation of the total matches and the number of documents that contain matches.
   */
  public Response count(String q,String corpora);
  
  /**
   * Find matches for a given AQL query.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/query/search/find</li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * produces:
   * <code>application/xml</code>:<br />
   * {@code
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <match-group>
   *   <!-- each match is enclosed in an match tag -->
   *   <match>
   *     <!-- the first matched node of match 1 did not match an annotation -->
   *     <anno></anno>
   *     <!-- the second matched node of match 1 was a match on the 'tiger::pos' annotation-->
   *     <anno>tiger::pos</anno>
   *     <!-- ID of first matched node of match 1 -->
   *     <id>salt:/pcc2/11299/#tok_1</id>
   *     <!-- ID of second matched noded  of match 1 -->
   *     <id>salt:/pcc2/11299/#tok_2</id>
   *   </match>
   *   <match>
   *     <anno></anno>
   *     <anno>tiger::pos</anno>
   *     <!-- ID of first matched noded of match 2 -->
   *     <id>salt:/pcc2/11299/#tok_2</id>
   *     <!-- ID of second matched noded of match 2-->
   *     <id>salt:/pcc2/11299/#tok_3</id>
   *   </match>
   *   <!-- and so on -->
   * </match-group>
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <match-group>
   * }
   * 
   * <i>or</i> produces:
   * <code>text/plain</code>:<br />
   * {@code
   * salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
   * salt:/pcc2/11299/#tok_2 tiger::pos::salt:/pcc2/11299/#tok_3
   * salt:/pcc2/11299/#tok_3 tiger::pos::salt:/pcc2/11299/#tok_4
   * }
   * One line per match, each ID is separated by space. An ID can be prepended
   * by the fully qualified annotation name (which is separated with '::' from the ID).
   * 
   * @param q The AQL query
   * @param corpora A comma separated list of corpus names
   * @param offset Optional offset from where to start the matches. Default is 0.
   * @param limit Optional limit of the number of returned matches. Set to -1 if unlimited. Default is -1.
   * @param order Optional order how the results should be sorted. Can be either "normal", "random" or "inverted"
   *  "normal" is the default ordering, "inverted" inverses the default ordering and "random" is a non-stable
   *  (thus you will get different results for the same offset and limit) random ordering.
   * @return
   * @throws IOException 
   */
  Response find(
    String q,
    String corpora,
    String offset,
    String limit,
    String order) throws IOException;
  
  /**
   * Get a graph as {@link SaltProject} from a set of (matched) Salt IDs.
   * <h3>Path(s)</h3>
   * <ol>
   * <li>POST annis/query/search/subgraph</li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * 
   * accepts:<br/>
   * <code>application/xml</code>:<br />
   * {@code
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <match-group>
   *   <!-- each match is enclosed in an match tag -->
   *   <match>
   *     <!-- the first matched node of match 1 did not match an annotation -->
   *     <anno></anno>
   *     <!-- the second matched node of match 1 was a match on the 'tiger::pos' annotation-->
   *     <anno>tiger::pos</anno>
   *     <!-- ID of first matched node of match 1 -->
   *     <id>salt:/pcc2/11299/#tok_1</id>
   *     <!-- ID of second matched noded  of match 1 -->
   *     <id>salt:/pcc2/11299/#tok_2</id>
   *   </match>
   *   <match>
   *     <anno></anno>
   *     <anno>tiger::pos</anno>
   *     <!-- ID of first matched noded of match 2 -->
   *     <id>salt:/pcc2/11299/#tok_2</id>
   *     <!-- ID of second matched noded of match 2-->
   *     <id>salt:/pcc2/11299/#tok_3</id>
   *   </match>
   *   <!-- and so on -->
   * </match-group>
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <match-group>
   * }
   * 
   * <i>or</i> accepts:
   * <code>text/plain</code>:<br />
   * {@code
   * salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
   * salt:/pcc2/11299/#tok_2 tiger::pos::salt:/pcc2/11299/#tok_3
   * salt:/pcc2/11299/#tok_3 tiger::pos::salt:/pcc2/11299/#tok_4
   * }
   * One line per match, each ID is separated by space. An ID can be prepended
   * by the fully qualified annotation name (which is separated with '::' from the ID).
   * 
   * produces:<br />
   * <code>application/xml</code> or <code>application/xmi+xml</code>:<br />
   * A representation of the Salt graph in the EMF XMI format.
   * 
   * 
   * @see #find(java.lang.String, java.lang.String, java.lang.String, java.lang.String) The output of find can be directly used by this function.
   * 
   * @param requestBody 
   * @param segmentation Optional parameter for segmentation layer on which the context is applied. Leave empty for token layer (which is default).
   * @param left Optional parameter for the left context size, default is 0.
   * @param right Optional parameter for the right context size, default is 0.
   * @param filter Optional parameter with value "all" or "token". 
   *  If "token" only token will be fetched. Default is "all".
   * @return the graph of this hit.
   */
  SaltProject subgraph(
    MatchGroup requestBody,
    String segmentation, String left, String right, String filter);
  
  /**
   * Get the annotation graph of a complete document.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/query/graph/<b>{top}</b>/<b>{doc}</b></li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * 
   * produces:<br />
   * <code>application/xml</code> or <code>application/xmi+xml</code>:<br />
   * A representation of the Salt graph in the EMF XMI format.
   * 
   * @param top The toplevel corpus.
   * @param doc The document.
   * @param filternodeanno A comma seperated list of node annotations which are 
   * used as a filter for the graph. Only nodes having one of the annotations
   * are included in the result.
   * @return 
   */
  public SaltProject graph(
    String top,
    String doc,
    String filternodeanno);
  
  /**
   * Get the content an ANNIS binary object for a specific document.
   *
   * <div>
   * There are several ways of selecting the binary data you want to recieve.
   * You can choose to select the file only by giving a document name.
   * This will return the first file that also matches your accepted mime types.
   * Additionally the name of the file itself can be given as path argument.
   * </div>
   * <div>
   * You can also choose to either get the complete file or
   * chunks containing only a subset of the binary data.
   * </div>
   *
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/query/corpora/<b>{top}</b>/<b>{document}</b>/binary/<b>{offset}</b>/<b>{length}</b></li>
   * <li>GET annis/query/corpora/<b>{top}</b>/<b>{document}</b>/binary</li>
   * <li>GET annis/query/corpora/<b>{top}</b>/<b>{document}</b>/binary/<b>{file}</b>/<b>{offset}</b>/<b>{length}</b></li>
   * <li>GET annis/query/corpora/<b>{top}</b>/<b>{document}</b>/binary/<b>{file}</b></li>
   * </ol>
   *
   * <h3>MIME</h3>
   * <div>
   * Accepts any mime type. The mime type is used as implicit argument to filter
   * the files that match a given query.
   * </div>
   *
   * @param top The toplevel corpus name.
   * @param document The name of the document that has the file. If you want the
   * files for the toplevel corpus itself, use the name of the toplevel corpus
   * as document name.
   * @param offset Defines the offset from the the binary chunk starts.
   * @param length Defines the length of the binary chunk.
   * @param file File name/title to select.
   * @return
   * A binary stream that contains the file content. If path variant 1 and 3
   * is used only a subset of the file is returned. Path variant 2 and 4 always
   * return the complete file.
   *
   * @see MetadataService#binaryMeta(java.lang.String, java.lang.String)
   */
  public Response binary(String top, String document, String offset, String length, String file);

  
}
