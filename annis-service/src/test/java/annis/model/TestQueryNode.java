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

import annis.sqlgen.model.RankTableJoin;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class TestQueryNode {
	
	// object under test
	private QueryNode node;
	

	@Before
	public void setup() {
		node = new QueryNode(0);
	}
	
	@Test
	public void qNameFullyQualified() {
		assertThat(QueryNode.qName("namespace", "name"), is("namespace:name"));
	}
	
	@Test
	public void qNameNoNamespace() {
		assertThat(QueryNode.qName(null, "name"), is("name"));
	}
	
	@Test
	public void setSpannedText() {
		// sanity check: values are null
		assertThat(node.getSpannedText(), is(nullValue()));
		
		// set span and text matching
		String spannedText = "span";
		node.setSpannedText(spannedText);
		
		// test functionality of setter
		assertThat(node.getSpannedText(), is(spannedText));
	}
	
	@Test
	public void clearSpannedText() {
		// set some values
		node.setSpannedText("span");
		
		// sanity check
		assertThat(node.getSpannedText(), is(not(nullValue())));
		
		// clear values
		node.clearSpannedText();
		
		// test for null values
		assertThat(node.getSpannedText(), is(nullValue()));
	}
	
	@Test
	public void addRelationRankTable() {
		// sanity check
		assertThat(node.isPartOfEdge(), is(false));
		
		// add a join that uses the rank table
		QueryNode target = new QueryNode(1);
		RankTableJoin rankTableJoin = new RankTableJoin(target, "foo", 0, 0) { };
		node.addOutgoingJoin(rankTableJoin);
		
		// assert both node and target know about the edge
		assertThat(node.isPartOfEdge(), is(true));
    assertThat(target.isPartOfEdge(), is(true));
	}

}
