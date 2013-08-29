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

import annis.model.QueryNode;


@SuppressWarnings("serial")
public class LeftDominance extends Dominance {

	public LeftDominance(QueryNode target) {
		super(target);
	}
	
	public LeftDominance(QueryNode target, String name) {
		super(target, name, 1);
	}
	
	@Override
	public String toString() {
		return "left-dominates node " + target.getId() + " (" + name + ")";
	}

  @Override
  public String toAqlOperator()
  {
    String domOp = super.toAqlOperator();
    return ">@l" + domOp.substring(1); // remove the ">" and replace with our own
  }
  
 

}
