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
package annis.visualizers.component.tree;

import annis.model.AnnisNode;
import java.util.Comparator;

public enum HorizontalOrientation {
	LEFT_TO_RIGHT(1),
	RIGHT_TO_LEFT(-1);
	
	private final int directionModifier;
	
	HorizontalOrientation(int directionModifier_) {
		directionModifier = directionModifier_;
	}
	
	Comparator<AnnisNode> getComparator() {
		return new Comparator<AnnisNode>() {
			@Override
			public int compare(AnnisNode o1, AnnisNode o2) {
        return directionModifier * (int) (o1.getLeftToken() - o2.getLeftToken());
			}
		};	
	}
}
