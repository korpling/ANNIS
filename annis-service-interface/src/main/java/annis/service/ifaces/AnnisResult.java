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
package annis.service.ifaces;

import annis.model.AnnotationGraph;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * This is the actual container for a single search result.
 * 
 * @author k.huetter
 * 
 */
public interface AnnisResult extends Serializable
{

	/** get Document Name */
	public String getDocumentName();

	/** get Document Name with complete path from toplevel corpus */
	public String[] getPath();

	/** id of last token */
	public long getEndNodeId();

	/** id of first token */
	public long getStartNodeId();

	/** ordered list of tokens */
	public List<AnnisToken> getTokenList();

	/** non-token annotation names */
	public Set<String> getAnnotationLevelSet();

	/** token annotation names */
	public Set<String> getTokenAnnotationLevelSet();

	/** get marker for node */
	public String getMarkerId(Long nodeId);

	/** is there a node with marker markerID **/
	public boolean hasMarker(String markerId);

	/** Get the underlying annotation graph */
	public AnnotationGraph getGraph();

}