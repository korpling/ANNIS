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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;

public class AnnisCorpusSetImpl extends HashSet<AnnisCorpus> implements Serializable, AnnisCorpusSet {
	

	public AnnisCorpusSetImpl() {
		super();
	}

	public AnnisCorpusSetImpl(Collection<? extends AnnisCorpus> collection) {
		super(collection);
	}

	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpusList#getJSON()
	 */
	public String getJSON() {
		StringBuffer json = new StringBuffer("{ \"size\":" + this.size() + ",\"list\":[");
		int count = 0;
		for(AnnisCorpus corpus : this) {
			if(count++ > 0)
				json.append(", ");
			json.append(corpus.getJSON());
		}
		return json.toString() + "]}";	
	}

}
