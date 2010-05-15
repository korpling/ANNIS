package annis.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import annis.model.AnnisNode.TextMatching;
import annis.sqlgen.model.RankTableJoin;

public class TestAnnisNode {
	
	// object under test
	private AnnisNode node;
	
	// arbitrary TextMatching value
	private static final TextMatching TEXT_MATCHING = TextMatching.EXACT_EQUAL;

	@Before
	public void setup() {
		node = new AnnisNode(0);
	}
	
	@Test
	public void qNameFullyQualified() {
		assertThat(AnnisNode.qName("namespace", "name"), is("namespace:name"));
	}
	
	@Test
	public void qNameNoNamespace() {
		assertThat(AnnisNode.qName(null, "name"), is("name"));
	}
	
	@Test
	public void setSpannedText() {
		// sanity check: values are null
		assertThat(node.getSpannedText(), is(nullValue()));
		assertThat(node.getSpanTextMatching(), is(nullValue()));
		
		// set span and text matching
		String spannedText = "span";
		TextMatching textMatching = TEXT_MATCHING;
		node.setSpannedText(spannedText, textMatching);
		
		// test functionality of setter
		assertThat(node.getSpannedText(), is(spannedText));
		assertThat(node.getSpanTextMatching(), is(textMatching));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setSpannedTextTextMatchingIsNull() {
		node.setSpannedText("span", null);
	}
	
	@Test
	public void clearSpannedText() {
		// set some values
		node.setSpannedText("span", TEXT_MATCHING);
		
		// sanity check
		assertThat(node.getSpannedText(), is(not(nullValue())));
		assertThat(node.getSpanTextMatching(), is(not(nullValue())));
		
		// clear values
		node.clearSpannedText();
		
		// test for null values
		assertThat(node.getSpannedText(), is(nullValue()));
		assertThat(node.getSpanTextMatching(), is(nullValue()));
	}
	
	@Test
	public void addRelationRankTable() {
		// sanity check
		assertThat(node.isPartOfEdge(), is(false));
		
		// add a join that uses the rank table
		AnnisNode target = mock(AnnisNode.class);
		RankTableJoin rankTableJoin = new RankTableJoin(target, "foo", 0, 0) { };
		node.addJoin(rankTableJoin);
		
		// assert both node and target know about the edge
		assertThat(node.isPartOfEdge(), is(true));
		verify(target).setPartOfEdge(true);
	}

	@Test
	public void setTokenIndexToken() {
		AnnisNode node = new AnnisNode(1);
		node.setTokenIndex(1L);
		assertThat(node.isToken(), is(true));
	}
	
	@Test
	public void setTokenIndexNull() {
		AnnisNode node = new AnnisNode(1);
		node.setTokenIndex(null);
		assertThat(node.isToken(), is(false));
	}
	
}
