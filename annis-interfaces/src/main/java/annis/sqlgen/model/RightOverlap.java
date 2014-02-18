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
package annis.sqlgen.model;

import annis.model.Join;
import annis.model.QueryNode;

public class RightOverlap extends Join {

	public RightOverlap(QueryNode target) {
		super(target);
	}

	@Override
	public String toString() {
		return "right overlaps node " + target.getId();
	}

  @Override
  public String toAqlOperator()
  {
    return "_or_";
  }
	
  
  
}
