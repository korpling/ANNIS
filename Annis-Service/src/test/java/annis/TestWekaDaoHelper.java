package annis;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.IsCollectionSize.size;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.dao.Match;
import annis.dao.Span;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;

public class TestWekaDaoHelper {

	private WekaDaoHelper wekaDaoHelper;
	
	// some nodes and ids
	private static final long ID1 = 1;
	private static final long ID2 = 2;
	private static final long ID3 = 3;
	
	private static final long TOKEN_INDEX = 1L;
	private static final String SPAN = "span";

	private AnnisNode annisNode1;
	private AnnisNode annisNode2;
	private AnnisNode annisNode3;

	// static Span objects, because they only hold data
	private static final Span SPAN1 = new Span(ID1, 0, 0, 0);
	private static final Span SPAN2 = new Span(ID2, 0, 0, 0);
	private static final Span SPAN3 = new Span(ID3, 0, 0, 0);
	
	// dummy annotation data
	private static final String NAMESPACE1 = "NAMESPACE1";
	private static final String NAMESPACE2 = "NAMESPACE2";
	private static final String NAMESPACE3 = "NAMESPACE3";
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	private static final String NAME3 = "NAME3";
	private static final String VALUE1 = "VALUE1";
	private static final String VALUE2 = "VALUE2";
	private static final String VALUE3 = "VALUE3";
	
	private static final Annotation annotation1 = new Annotation(NAMESPACE1, NAME1, VALUE1, TextMatching.EXACT_EQUAL);
	private static final Annotation annotation2 = new Annotation(NAMESPACE2, NAME2, VALUE2, TextMatching.EXACT_EQUAL);
	private static final Annotation annotation3 = new Annotation(NAMESPACE3, NAME3, VALUE3, TextMatching.EXACT_EQUAL);

	private static final String NULL = "NULL";
	
	// some matches
	private Match match1;
	private Match match2;

	@Before
	public void setup() {
		annisNode1 = new AnnisNode(ID1);
		annisNode2 = new AnnisNode(ID2);
		annisNode3 = new AnnisNode(ID3);

		match1 = new Match();
		match2 = new Match();
	
		wekaDaoHelper = new WekaDaoHelper();
		wekaDaoHelper.setNullValue(NULL);
	}
	
	@Test
	public void createSqlQuery() {
		// 1st match: nodes 1 and 2
		match1.add(SPAN1);
		match1.add(SPAN2);

		// 2nd match: node 3
		match2.add(SPAN3);

		// a list of matches with match1, match2
		List<Match> matches = Arrays.asList(match1, match2);

		String expected = "SELECT node.id, node.token_index, node.span, node_annotation.* FROM node, node_annotation WHERE node.id IN ( 1, 2, 3 ) and node_annotation.node_ref = node.id";
		
		// sanity check: make sure, expected IDs are correct
		assertThat(ID1 + ", " + ID2 + ", " + ID3, is("1, 2, 3"));

		// call and test
		assertThat(wekaDaoHelper.createSqlQuery(matches), is(expected));
	}
	
	@Test
	public void extractData() throws SQLException {
		// stub a ResultSet to return 3 rows from node_annotation
		// 1st and 3rd row: annotations for ID1
		// 2nd row: annotation for ID2
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getLong("token_index")).thenReturn(TOKEN_INDEX, 0L, TOKEN_INDEX);
		when(resultSet.wasNull()).thenReturn(false, true, false);
		when(resultSet.getString(SPAN)).thenReturn(SPAN, null, SPAN);
		when(resultSet.getLong("node_ref")).thenReturn(ID1, ID2, ID1);
		when(resultSet.getString("namespace")).thenReturn(NAMESPACE1, NAMESPACE2, NAMESPACE3);
		when(resultSet.getString("name")).thenReturn(NAME1, NAME2, NAME3);
		when(resultSet.getString("value")).thenReturn(VALUE1, VALUE2, VALUE3);
		
		// expected values
		annisNode1.setTokenIndex(TOKEN_INDEX);
		annisNode1.setSpannedText(SPAN);
		annisNode1.addNodeAnnotation(annotation1);
		annisNode1.addNodeAnnotation(annotation3);
		annisNode2.addNodeAnnotation(annotation2);
			
		// call and test
		List<AnnisNode> annisNodes = wekaDaoHelper.extractData(resultSet);
		assertThat(annisNodes, is(size(2)));
		assertThat(annisNodes, hasItems(annisNode1, annisNode2));
	}
	
	@Test
	public void exportAsWeka() {
		// 1st match: nodes 1 and 2
		match1.add(SPAN1);
		match1.add(SPAN2);

		// 2nd match: node 3
		match2.add(SPAN3);
		
		// node 1 has annotations 1 and 2
		annisNode1.addNodeAnnotation(annotation1);
		annisNode1.addNodeAnnotation(annotation2);
		
		// node 2 has annotation 3
		annisNode2.addNodeAnnotation(annotation1);
		
		// node 3 has annotation 1 and 3, and is a token
		annisNode3.addNodeAnnotation(annotation1);
		annisNode3.addNodeAnnotation(annotation3);
		annisNode3.setTokenIndex(1L);
		annisNode3.setSpannedText("span");
		
		String expected = "" +
			"@relation name\n" +
			"\n" +
			"@attribute #1_id string\n" +
			"@attribute #1_token string\n" +
			"@attribute #1_NAMESPACE1:NAME1 string\n" +
			"@attribute #1_NAMESPACE2:NAME2 string\n" +
			"@attribute #1_NAMESPACE3:NAME3 string\n" +
			"@attribute #2_id string\n" +
			"@attribute #2_token string\n" +
			"@attribute #2_NAMESPACE1:NAME1 string\n" +
			"\n" +
			"@data\n" +
			"\n" +
			"'1','NULL','VALUE1','VALUE2','NULL','2','NULL','VALUE1'\n" +
			"'3','span','VALUE1','NULL','VALUE3','NULL','NULL','NULL'\n" +
			"";
		
		String actual = wekaDaoHelper.exportAsWeka(
				Arrays.asList(annisNode1, annisNode2, annisNode3), 
				Arrays.asList(match1, match2));

		assertEquals(expected, actual);
	}
	
}

