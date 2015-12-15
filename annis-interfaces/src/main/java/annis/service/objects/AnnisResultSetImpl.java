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

import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class AnnisResultSetImpl extends TreeSet<AnnisResult> implements AnnisResultSet {

	private static class AnnisResultSetComparator implements Comparator<AnnisResult>, Serializable {
		

    @Override
		public int compare(AnnisResult o1, AnnisResult o2) {
			int order =  Long.signum(o1.getStartNodeId() - o2.getStartNodeId());
			if (order == 0) {
				order = Long.signum(o1.getEndNodeId() - o2.getEndNodeId());
				if (order == 0)
					return -1;
				else
					return order;
			} else 
				return order;
		}
	}

	

	public AnnisResultSetImpl() {
		super(new AnnisResultSetComparator());
	}
	
	public AnnisResultSetImpl(Collection<? extends AnnisResult> collection) {
		this();
		addAll(collection);
	}

  @Override
	public Set<String> getAnnotationLevelSet() {
		Set<String> levelSet = new HashSet<String>();
		for(AnnisResult result : this)
			levelSet.addAll(result.getAnnotationLevelSet());
		return levelSet;
	}

  @Override
	public Set<String> getTokenAnnotationLevelSet() {
		Set<String> levelSet = new HashSet<String>();
		for(AnnisResult result : this)
			levelSet.addAll(result.getTokenAnnotationLevelSet());
		return levelSet;
	}

}
