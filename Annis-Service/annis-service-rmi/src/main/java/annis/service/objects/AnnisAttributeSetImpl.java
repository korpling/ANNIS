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

import java.util.Collection;
import java.util.HashSet;

import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;

public class AnnisAttributeSetImpl extends HashSet<AnnisAttribute> implements AnnisAttributeSet {
	

	public AnnisAttributeSetImpl() {
		super();
	}

	public AnnisAttributeSetImpl(Collection<? extends AnnisAttribute> c) {
		super(c);
	}

	public String getJSON() {
		StringBuffer sBuffer = new StringBuffer();
		for(AnnisAttribute a : this) {
			if(sBuffer.length() > 0)
				sBuffer.append(", \n");
			sBuffer.append(a.getJSON());
		}
		return "[" + sBuffer + "]\n";
	}
}
