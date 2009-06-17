package annis.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestAnnotationGraph {

	// object under test
	private AnnotationGraph graph;
	
	@Before
	public void setup() {
		graph = new AnnotationGraph();
	}
	
	// getTokens() returns tokens in order
	@Test
	public void addNodeToken() {
		// a few tokens
		AnnisNode token1 = newMockedToken(1);
		AnnisNode token2 = newMockedToken(2);
		AnnisNode token3 = newMockedToken(3);
		
		// a few non-tokens
		AnnisNode node1 = new AnnisNode(1);
		AnnisNode node2 = new AnnisNode(2);
		
		// add nodes, tokens are out of order
		graph.addNode(token2);
		graph.addNode(token1);
		graph.addNode(node2);
		graph.addNode(node1);
		graph.addNode(token3);
		
		// test
		List<AnnisNode> expected = Arrays.asList(token1, token2, token3);
		assertThat(graph.getTokens(), is(expected));
	}
	
	// addNode() stores graph in AnnisNode
	@Test
	public void addNodeStoresGraphInNode() {
		AnnisNode node = new AnnisNode(1);
		graph.addNode(node);
		assertThat(node.getGraph(), is(graph));
	}
	
	// addNode() stores tokens by tokenIndex
	@Test
	public void addNodeStoresTokenByIndex() {
		// create a token and add it to the graph
		final long TOKEN_INDEX = 23L;
		AnnisNode token = newMockedToken(TOKEN_INDEX);
		graph.addNode(token);
		
		// query the token by its token index
		assertThat(graph.getToken(TOKEN_INDEX), is(token));
	}
	
	private AnnisNode newMockedToken(long tokenIndex) {
		AnnisNode node = mock(AnnisNode.class);
		when(node.getTokenIndex()).thenReturn(tokenIndex);
		when(node.isToken()).thenReturn(true);
		return node;
	}
	
}
