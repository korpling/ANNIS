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

import annis.service.objects.AnnisBinaryMetaData;
import java.util.List;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Interface defining the REST API calls that ANNIS provides for querying the
 * data.
 * 
 * <div>
 * All paths for this part of the service start with <pre>annis/query/</pre>
 * </div>
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public interface QueryService
{
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
   * @see #binaryMeta(java.lang.String, java.lang.String) 
   */
  public Response binary(String top, String document, String offset, String length, String file);
  
  /**
   * Get the metadata of all ANNIS binary objects for a specific document.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET corpora/<b>{top}</b>/<b>{document}</b>/binary/meta</li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * <div>
   * Accepts application/xml
   * </div>
   * 
   * @param top The toplevel corpus name.
   * @param document The name of the document that has the file. If you want the
   * files for the toplevel corpus itself, use the name of the toplevel corpus
   * as document name.
   * @return A XML representation containing a list of all 
   *  matching {@link annis.service.objects.AnnisBinaryMetaData} objects.
   */
  public List<AnnisBinaryMetaData> binaryMeta(String toplevelCorpusName, String documentName);
}
