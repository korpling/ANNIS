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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisToken;
import static annis.test.TestUtils.size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItems;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestAnnisResultImpl {

	// AnnisResultImpl wraps an underlying annotation graph
	@Mock private AnnotationGraph graph;

	// some token example data
	private static final long LEFT = 201;
	private static final long RIGHT = 301;

	private static final long ID1 = 646;
	private static final String TEXT1 = "Hello";
	private static final long TOKEN_INDEX1 = 1;
	
	private static final long ID2 = 254;
	private static final String TEXT2 = "world";
	private static final long TOKEN_INDEX2 = 2;
	
	private static final long ID3 = 103;
	private static final String TEXT3 = "!";
	private static final long TOKEN_INDEX3 = 3;

	private AnnisNode token1;
	private AnnisNode token2;
	private AnnisNode token3;
	
	// some other node data
	private static final long ID4 = 104;
	private static final long ID5 = 105;
	
	private AnnisNode node4;
	private AnnisNode node5;
	
	// some annotation data
	private static final String NAMESPACE = "NAMESPACE";
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	
	
	@Before
	public void setup() {
		initMocks(this);

		// sanity check: IDS are different
		assertThat(ID1, is(not(ID2)));
		assertThat(ID2, is(not(ID3)));
		assertThat(ID3, is(not(ID1)));
		
		// set up underlying graph
		token1 = newToken(ID1, TEXT1, TOKEN_INDEX1);
		token2 = newToken(ID2, TEXT2, TOKEN_INDEX2);
		token3 = newToken(ID3, TEXT3, TOKEN_INDEX3);
		node4 = new AnnisNode(ID4);
		node5 = new AnnisNode(ID5);
		when(graph.getNodes()).thenReturn(Arrays.asList(token1, token2, token3, node4, node5));
		when(graph.getTokens()).thenReturn(Arrays.asList(token1, token2, token3));
	}
	
	// convert token list
	@Test
	public void getTokenList() {
		// expected
		List<AnnisToken> expected = new ArrayList<>();
		expected.add(new AnnisTokenImpl(ID1, TEXT1, LEFT, RIGHT, TOKEN_INDEX1, 1L));
		expected.add(new AnnisTokenImpl(ID2, TEXT2, LEFT, RIGHT, TOKEN_INDEX2, 1L));
		expected.add(new AnnisTokenImpl(ID3, TEXT3, LEFT, RIGHT, TOKEN_INDEX3, 1L));

		// wrap and test
		AnnisResult annisResult = new AnnisResultImpl(graph);
		assertThat(annisResult.getTokenList(), is(expected));
	}
	
	// return last token
	@Test
	public void getStartEndNodeId() {
		// wrap and test
		AnnisResult annisResult = new AnnisResultImpl(graph);
		assertThat(annisResult.getEndNodeId(), is(ID3));
	}
	
	// return annotation names of non-tokens
	@Test
	public void getAnnotationLevelSet() {
		// add annotation data to tokens and nodes
		token3.addNodeAnnotation(new Annotation(NAMESPACE, NAME1));
		node5.addNodeAnnotation(new Annotation(NAMESPACE, NAME2));
		
		// wrap and test: only the annotation for node5 is returned
		AnnisResult annisResult = new AnnisResultImpl(graph);
		Set<String> annotations = annisResult.getAnnotationLevelSet();
		assertThat(annotations, size(1));
		assertThat(annotations, hasItems(AnnisNode.qName(NAMESPACE, NAME2)));
	}
	
	// return annotation names of tokens
	@Test
	public void getTokenAnnotationLevelSet() {
		// add annotation data to tokens and nodes
		token3.addNodeAnnotation(new Annotation(NAMESPACE, NAME1));
		node5.addNodeAnnotation(new Annotation(NAMESPACE, NAME2));
		
		// wrap and test: only the annotation for token3 is returned
		AnnisResult annisResult = new AnnisResultImpl(graph);
		Set<String> tokenAnnotations = annisResult.getTokenAnnotationLevelSet();
		assertThat(tokenAnnotations, size(1));
		assertThat(tokenAnnotations, hasItems(AnnisNode.qName(NAMESPACE, NAME1)));
	}
	
	// return ID (as string) if node is a matched node, otherwise return null
	@Test
	public void getMarkerIdMatchedNode() {
		// underlying graph has marker for node ID1
		when(graph.getMatchedNodeIds()).thenReturn(new HashSet<>(Arrays.asList(ID1)));
		
		// wrap and test: ID1 is marked, ID2 is not
		AnnisResult annisResult = new AnnisResultImpl(graph);
		assertThat(annisResult.getMarkerId(ID1), is(String.valueOf(ID1)));
		assertThat(annisResult.getMarkerId(ID2), is(nullValue()));
	}

	// a node is marked if it is a matched node
	@Test
	public void hasMarkerId() {
		// underlying graph has marker for node ID1
		when(graph.getMatchedNodeIds()).thenReturn(new HashSet<>(Arrays.asList(ID1)));
		
		// wrap and test: ID1 is marked, ID2 is not
		AnnisResult annisResult = new AnnisResultImpl(graph);
		assertThat(annisResult.hasMarker(String.valueOf(ID1)), is(true));
		assertThat(annisResult.hasMarker(String.valueOf(ID2)), is(false));
	}

	///// private helper
	
	private AnnisNode newToken(long id, String text, long tokenIndex) {
		AnnisNode n = new AnnisNode(id);
		n.setSpannedText(text);
		n.setTokenIndex(tokenIndex);
		n.setLeft(LEFT);
		n.setRight(RIGHT);
		return n;
	}
}
