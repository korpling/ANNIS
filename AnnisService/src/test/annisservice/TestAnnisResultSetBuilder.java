package annisservice;

import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionSize.size;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

import annisservice.ifaces.AnnisResult;
import annisservice.ifaces.AnnisResultSet;
import annisservice.ifaces.AnnisToken;
import annisservice.objects.AnnisResultImpl;

public class TestAnnisResultSetBuilder {

	class MockResultSet extends de.deutschdiachrondigital.dddquery.sql.MockResultSet {
		
		Object[][] rows;
		
		int i = -1;
		
		public MockResultSet(Object[]... rows) {
			this.rows = rows;
		}
		
		@Override
		public boolean next() {
			return ++i < rows.length;
		}
		
		@Override
		public String getString(int index) {
			return (String) getField(index);
		}
		
		@Override
		public long getLong(int index) {
			return (Long) getField(index);
		}

		private Object getField(int index) {
			return rows[i][index - 1];
		}

	};
		
	class MockAnnisResultSetBuilder extends AnnisResultSetBuilder {
		List<Object> calls = new ArrayList<Object>();
		@Override
		void newAnnisResult() {
			calls.add("newAnnisResult");
		}
		@Override
		void startNode(boolean leaf, long id, String name, Long textRef, long left, long right, String text, long tokenIndex) {
			calls.add("startNode");
			calls.add(leaf);
		}
		@Override
		void closeNode() {
			calls.add("closeNode");
		}
		@Override
		void addAnnotation(String attribute, String value) {
			calls.add("addAnnotation");
			calls.add(attribute);
			calls.add(value);
		}
	}
	
	class Row {
		String key;
		long pre;
		long post;
		long struct_id = randomLong();
		String name = uniqueString();
		long text_ref = randomLong();
		long left = randomLong();
		long right = randomLong();
		long token_left = randomLong();
		long token_right = randomLong();
		String span = uniqueString();
		String attribute = uniqueString();
		String value = uniqueString();
		
		boolean isTerminal() {
			return pre == post - 1;
		}
		
		ResultSet getResultSet() {
			MockResultSet resultSet = new MockResultSet(new Object[] {
					key, pre, post, 
					struct_id, name, 
					text_ref, left, right, token_left, token_right, span, 
					attribute, value
			});
			resultSet.next();
			return resultSet;
		}
	}
	
	/*
	 * first row is a special case of new key
	 * - new AnnisREsult
	 * - new Node
	 * - new Annotation
	 * - push post on stack
	 */
	@Test
	public void processRowFirstRow() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		Row row = newAnonymousRow(uniqueString(), 1, 10);
		
		builder.processRow(row.getResultSet());
		
		assertThat(builder.calls, isCollection("newAnnisResult", "startNode", row.isTerminal(), "addAnnotation", row.attribute, row.value));
		assertThat(builder.getPostStack(), isCollection(row.post));
	}
	
//	@Before
//	public void setup() {
//		MockitoAnnotations.initMocks(this);
//	}
//	
//	@Mock private AnnisResultSetBuilder builder2;
//	
//	@Test
//	public void processRowFirstRowMocked() throws SQLException {
//		Row row = newAnonymousRow(uniqueString(), 1, 10);
//		
//		builder2.processRow(row.getResultSet());
//		
//		fail("need to abstract AnnisResultSetBuilder from JdbcResultSetParser");
//		
//		InOrder inOrder = inOrder(builder2);
//		inOrder.verify(builder2).newAnnisResult();
//		inOrder.verify(builder2).startNode(row.isTerminal(), row.struct_id, row.name, row.text_ref, row.left, row.right, row.span, row.token_left);
//		inOrder.verify(builder2).addAnnotation(row.attribute, row.value);
//		verifyNoMoreInteractions(builder2);
//		
//		assertThat(builder2.getPostStack(), isCollection(row.post));
//	}

	/*
	 * new key -> new AnnisResult
	 * - same as first row, however the post stack is cleared before pushing the current post
	 */
	@Test
	public void processRowNewKey() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		processAnonymousRow(builder, 1, 10);
		Row row = newAnonymousRow(uniqueString(), 11, 20);

		builder.processRow(row.getResultSet());
		
		assertThat(builder.calls, isCollection("newAnnisResult", "startNode", row.isTerminal(), "addAnnotation", row.attribute, row.value));
		assertThat(builder.getPostStack(), isCollection(row.post));
	}
	
	/*
	 * same key, same pre -> another annotation on current node
	 * - add annotation
	 * - don't push post
	 */
	@Test
	public void processRowSameNode() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		Row row1 = processAnonymousRow(builder, 1, 10);
		Row row2 = newAnonymousRow(row1.key, 1, 10);

		builder.processRow(row2.getResultSet());
		
		assertThat(builder.calls, isCollection("addAnnotation", row2.attribute, row2.value));
		assertThat(builder.getPostStack(), isCollection(row1.post));
	}

	/*
	 * same key, different pre; pre < last post -> child node of currently open node
	 * - new node
	 * - add annotation
	 * - push post to stack
	 */
	@Test
	public void processRowChildNode() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		Row row1 = processAnonymousRow(builder, 1, 10);
		Row row2 = newAnonymousRow(row1.key, 2, 9);
		
		builder.processRow(row2.getResultSet());
		
		assertThat(builder.calls, isCollection("startNode", row2.isTerminal(), "addAnnotation", row2.attribute, row2.value));
		assertThat(builder.getPostStack(), isCollection(row1.post, row2.post));
	}
	
	/*
	 * same key, different pre; pre > last post -> node along the following (xpath) axis, loop until pre < last post
	 * - close node
	 * - new node
	 * - add annotation
	 * - pop last post from stack
	 * - push post to stack
	 */
	@Test
	public void processNodeFollowingNode() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		Row row1 = processAnonymousRow(builder, 1, 10);
		Row row2 = newAnonymousRow(row1.key, 11, 20);

		builder.processRow(row2.getResultSet());
		
		assertThat(builder.calls, isCollection("closeNode", "startNode", row2.isTerminal(), "addAnnotation", row2.attribute, row2.value));
		assertThat(builder.getPostStack(), isCollection(row2.post));
	}

	/*
	 * same as followingNode, but the builder has seen more rows by now
	 * - node1, pre = 1, post = 2
	 * - node2, 3, 12
	 * 		- node3, 4, 9
	 * 			- node4, 5, 8
	 * 				- node5, 6, 7
	 * 		- node6, 10, 11		(<- builder processes this: node5, 4 and 3 must be closed, before new node is started)
	 */
	@Test
	public void processNodeFollowingNodeComplext() throws SQLException {
		MockAnnisResultSetBuilder builder = new MockAnnisResultSetBuilder();
		Row row1 = newAnonymousRow(uniqueString(), 1, 2);
		Row row2 = newAnonymousRow(row1.key, 3, 12);
		Row row3 = newAnonymousRow(row1.key, 4, 9);
		Row row4 = newAnonymousRow(row1.key, 5, 8);
		Row row5 = newAnonymousRow(row1.key, 6, 7);
		Row row6 = newAnonymousRow(row1.key, 10, 11);
		processAnonymousRows(builder, row1, row2, row3, row4, row5);
		
		builder.processRow(row6.getResultSet());
		
		assertThat(builder.calls, isCollection("closeNode", "closeNode", "closeNode", "startNode", row6.isTerminal(), "addAnnotation", row6.attribute, row6.value));
		assertThat(builder.getPostStack(), isCollection(row2.post, row6.post));
	}
	
	@Test
	public void newAnnisResult() {
		AnnisResultSetBuilder builder = new AnnisResultSetBuilder();
		
		builder.newAnnisResult();
		AnnisResultSet annisResultSet = builder.getAnnisResultSet();
		AnnisResult annisResult = builder.getAnnisResult();
		
		assertThat(annisResult, is(not(nullValue())));
		assertThat(annisResultSet, hasItem(annisResult));
	}
	
	@Test
	public void startNode() throws ParserConfigurationException {
		// FIXME: refactor
		AnnisResultSetBuilder builder = new AnnisResultSetBuilder();
		AnnisResultImpl annisResult = new AnnisResultImpl();
		builder.setAnnisResult(annisResult);
		builder.setLastKey("1234L");
		builder.setPaulaDom(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
		builder.getElementStack().add(builder.getPaulaDom().createElement("RESULT"));
		
		builder.startNode(true, 1234L, uniqueString(), 1L, 10L, 20L, "what", 8L);
		
		assertThat(annisResult.getTextId(), is(1L));

		assertThat(annisResult.getTokenList(), size(1));
		AnnisToken annisToken = annisResult.getTokenList().get(0);
		
		assertThat(annisToken.getId(), is(1234L));
		assertThat(annisToken.getLeft(), is(10L));
		assertThat(annisToken.getRight(), is(20L));
		assertThat(annisToken.getText(), is("what"));
		assertThat(annisToken.getTokenIndex(), is(8L));
		
		fail("implement me: markierungen");
	}
	
	@Test
	public void complexExample() {
		// FIXME: cleanup, document
		Object[] row1 = { "{5172,5173,5174}", 1L, 2L, 5171L, "STRUCT", 13L, 128L, 129L, 21L, 21L, ",", "tiger:pos", "DOLLAR_COMMA", null };
		Object[] row2 = { "{5172,5173,5174}", 3L, 16L, 5427L, "STRUCT", 13L, 113L, 292L, 18L, 53L, null, "tiger:cat", "NP", null };
		Object[] row3 = { "{5172,5173,5174}", 4L, 15L, 5424L, "STRUCT", 13L, 130L, 292L, 22L, 53L, null, "tiger:cat", "S", "RE" };
		Object[] row4 = { "{5172,5173,5174}", 5L, 6L, 5172L, "STRUCT", 13L, 130L, 134L, 22L, 22L, "wenn", "tiger:pos", "KOUS", "CP" };
		Object[] row5 = { "{5172,5173,5174}", 5L, 6L, 5172L, "STRUCT", 13L, 130L, 134L, 22L, 22L, "wenn", "tiger:lemma", "wenn", "CP" };
		Object[] row6 = { "{5172,5173,5174}", 7L, 8L, 5173L, "STRUCT", 13L, 135L, 138L, 23L, 23L, "man", "tiger:pos", "PIS", "SB" };
		Object[] row7 = { "{5172,5173,5174}", 7L, 8L, 5173L, "STRUCT", 13L, 135L, 138L, 23L, 23L, "man", "tiger:lemma", "man", "SB" };
		Object[] row8 = { "{5172,5173,5174}", 9L, 10L, 5174L, "STRUCT", 13L, 139L, 143L, 24L, 24L, "sich", "tiger:pos", "PRF", "DA" };
		Object[] row9 = { "{5172,5173,5174}", 9L, 10L, 5174L, "STRUCT", 13L, 139L, 143L, 24L, 24L, "sich", "tiger:lemma", "sich", "DA" };
		Object[] row10 = { "{5172,5173,5174}", 11L, 14L, 5372L, "STRUCT", 13L, 144L, 152L, 25L, 26L, null, "tiger:cat", "NP", "OA" };
		Object[] row11 = { "{5172,5173,5174}", 12L, 13L, 5175L, "STRUCT", 13L, 144L, 147L, 25L, 25L, "den", "tiger:pos", "ART", "NK" };
		Object[] row12 = { "{5172,5173,5174}", 12L, 13L, 5175L, "STRUCT", 13L, 144L, 147L, 25L, 25L, "den", "tiger:lemma", "den", "NK" };
		ResultSet resultSet = new MockResultSet(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10, row11, row12);
		
		AnnisResultSetBuilder builder = new AnnisResultSetBuilder();
		AnnisResultSet annisResultSet = builder.buildResultSet(resultSet);
		
		assertThat(annisResultSet, size(1));
		
		AnnisResult annisResult = annisResultSet.iterator().next();
		List<AnnisToken> tokens = annisResult.getTokenList();
		
		assertThat(tokens, size(5));
		
		String[] expectedTokens = { ",", "wenn", "man", "sich", "den" };
		for (int i = 0; i < tokens.size(); ++i)
			assertThat(tokens.get(i).getText(), is(expectedTokens[i]));
		
		assertThat(annisResult.getStartNodeId(), is(5171L));
		assertThat(annisResult.getEndNodeId(), is(5175L));
		
		List<Long> ids = Arrays.asList(5171L, 5427L, 5424L, 5172L, 5173L, 5174L, 5372L, 5175L);
		List<Long> marked = Arrays.asList(5172L, 5173L, 5174L);
		for (long id : ids) {
			boolean isMarked = marked.contains(id);
			String mark = "n" + id;
			assertThat(mark, annisResult.hasMarker(mark), is(isMarked));
			assertThat(mark, annisResult.hasNodeMarker(id), is(isMarked));
			if (isMarked) {
				assertThat(mark, annisResult.getMarkerId(id), is(mark));
				assertThat(mark, annisResult.getNodeId(mark), is(id));
			}
		}
		
		System.out.println(annisResult.getPaula());
		
		fail("implement me: corpus, text, paula, 2. resultset");
	}	
		
	private Row newAnonymousRow(String key, long pre, long post) {
		Row row = new Row();
		row.key = key;
		row.pre = pre;
		row.post = post;
		return row;
	}

	/*
	 * - let the builder process some rows
	 * - reset the call history of the builder
	 * - return the processed row for reference
	 */
	private void processAnonymousRows(MockAnnisResultSetBuilder builder, Row... rows)
	throws SQLException {
		for (Row row : rows)
			builder.processRow(row.getResultSet());
		builder.calls.clear();
	}
	
	private Row processAnonymousRow(MockAnnisResultSetBuilder builder, int pre,
			int post) throws SQLException {
		Row row = newAnonymousRow(uniqueString(), pre, post);
		processAnonymousRows(builder, row);
		return row;
	}
	
	private int stringIndex = 0;
	
	private String uniqueString() {
		return "unique" + ++stringIndex;
	}
	
	private long randomLong() {
		final int MAX_KEYS = 10;
		return (long) (Math.random() * (MAX_KEYS - 1)) + 1;
	}

}
