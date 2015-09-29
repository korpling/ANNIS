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
package annis.model;

import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("serial")
public abstract class Join extends DataObject {

	protected QueryNode target;
  private Set<QueryAnnotation> edgeAnnotations;
  
  private ParsedEntityLocation parseLocation;

	public Join(QueryNode target) {
		this.target = target;
    this.edgeAnnotations = new TreeSet<>();
	}

	public QueryNode getTarget() {
		return target;
	}
  
  public boolean addEdgeAnnotation(QueryAnnotation anno)
  {
    return edgeAnnotations.add(anno);
  }

  public Set<QueryAnnotation> getEdgeAnnotations()
  {
    return edgeAnnotations;
  }

  public void setEdgeAnnotations(Set<QueryAnnotation> edgeAnnotations)
  {
    this.edgeAnnotations = edgeAnnotations;
  }
  
  public String toAQLFragment(QueryNode source)
  {
    return "#" + source.getVariable() +" " + toAqlOperator() + " #" + target.getVariable();
  }
  
  public String toAqlOperator()
  {
    return "X";
  }

  public ParsedEntityLocation getParseLocation()
  {
    return parseLocation;
  }

  public void setParseLocation(ParsedEntityLocation parseLocation)
  {
    this.parseLocation = parseLocation;
  }
  
  
  
}