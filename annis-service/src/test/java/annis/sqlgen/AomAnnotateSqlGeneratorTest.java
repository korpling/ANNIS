package annis.sqlgen;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class AomAnnotateSqlGeneratorTest
{

  // object under test
  private AomAnnotateExtractor generator;

  // dependencies
  @Mock
  private TableAccessStrategy tableAccessStrategy;

  // test data
  @Mock private ResultSet resultSet;
  
  // node table test data
  private static final long ID = uniqueInt();
  private static final long TEXT_REF = uniqueInt();
  private static final long CORPUS_REF = uniqueInt();
  private static final String NODE_NAMESPACE = uniqueString("NAMESPACE");
  private static final String NODE_NAME = uniqueString("NAME");
  private static final long LEFT = uniqueInt();
  private static final long RIGHT = uniqueInt();
  private static final long TOKEN_INDEX = uniqueInt();
  private static final long LEFT_TOKEN = uniqueInt();
  private static final long RIGHT_TOKEN = uniqueInt();
  private static final String SPAN = uniqueString("SPAN");
  
  // rank and component table test data
  private static final long PRE = uniqueInt();
  private static final EdgeType EDGETYPE = EdgeType.UNKNOWN;
  private static final String EDGE_NAMESPACE = uniqueString("NAMESPACE");
  private static final String EDGE_NAME = uniqueString("NAME");
  private static final long PARENT = uniqueInt();
  private static final long NODE_REF = uniqueInt();

  // annotation test tables
  private static final String NAMESPACE = "NAMESPACE";
  private static final String NAME = "NAME";
  private static final String VALUE = "VALUE";

  @Before
  public void setup()
  {
    initMocks(this);
    generator = new AomAnnotateExtractor();
  }

  @Test
  public void shouldMapNode() throws SQLException
  {
    // given
    stubNodeResultSet();
    // when
    AnnisNode actual = generator.mapNode(resultSet, tableAccessStrategy, null);
    // then
    AnnisNode expected = new AnnisNode(ID, CORPUS_REF, TEXT_REF, LEFT, RIGHT, NODE_NAMESPACE, NODE_NAME, TOKEN_INDEX, SPAN, LEFT_TOKEN, RIGHT_TOKEN);
    assertThat(actual, is(expected));
  }
  
  @Test
  public void shouldMapNodeWithToken() throws SQLException
  {
    // given
    stubNodeResultSet();
    given(resultSet.wasNull()).willReturn(true);
    // when
    AnnisNode node = generator.mapNode(resultSet, tableAccessStrategy, null);
    // then
    assertThat(node.isToken(), is(false));
  }
  
  @Test
  public void shouldMapNodeWithoutToken() throws SQLException
  {
    // given
    stubNodeResultSet();
    given(resultSet.wasNull()).willReturn(false);
    // when
    AnnisNode node = generator.mapNode(resultSet, tableAccessStrategy, null);
    // then
    assertThat(node.isToken(), is(true));
  }
  
  @Test
  public void shouldMapEdge() throws SQLException
  {
    // given
    stubEdgeResultSet();
    // when
    Edge actual = generator.mapEdge(resultSet, tableAccessStrategy);
    // then
    Edge expected = createDefaultEdge();
    assertThat(actual, is(expected));
  }

  @Test
  public void shouldMapEdgeWithRoot() throws SQLException
  {
    // given
    stubEdgeResultSet();
    given(resultSet.wasNull()).willReturn(true);
    // when
    Edge actual = generator.mapEdge(resultSet, tableAccessStrategy);
    // then
    Edge expected = createDefaultEdge();
    expected.setSource(null);
    assertThat(actual, is(expected));
  }
  
  @Test
  public void shouldMapAnnotation() throws SQLException
  {
    // given
    String table = uniqueString();
    stubDefaultAnnotation(table);
    // when
    Annotation actual = generator.mapAnnotation(resultSet, tableAccessStrategy, table);
    // then
    Annotation expected = new Annotation(NAMESPACE, NAME, VALUE);
    assertThat(actual, is(expected));
  }

  @Test
  public void shouldMapEmptyAnnotation() throws SQLException
  {
    // given
    String table = uniqueString();
    stubDefaultAnnotation(table);
    given(resultSet.wasNull()).willReturn(true);
    // when
    Annotation actual = generator.mapAnnotation(resultSet, tableAccessStrategy, table);
    // then
    assertThat(actual, is(nullValue()));
  }

  private void stubNodeResultSet() throws SQLException
  {
    stubLongColumn(NODE_TABLE, "id", ID);
    stubLongColumn(NODE_TABLE, "corpus_ref", CORPUS_REF);
    stubLongColumn(NODE_TABLE, "text_ref", TEXT_REF);
    stubLongColumn(NODE_TABLE, "left", LEFT);
    stubLongColumn(NODE_TABLE, "right", RIGHT);
    stubStringColumn(NODE_TABLE, "namespace", NODE_NAMESPACE);
    stubStringColumn(NODE_TABLE, "name", NODE_NAME);
    stubLongColumn(NODE_TABLE, "token_index", TOKEN_INDEX);
    stubStringColumn(NODE_TABLE, "span", SPAN);
    stubLongColumn(NODE_TABLE, "left_token", LEFT_TOKEN);
    stubLongColumn(NODE_TABLE, "right_token", RIGHT_TOKEN);
  }

  private void stubEdgeResultSet() throws SQLException
  {
    stubLongColumn(RANK_TABLE, "pre", PRE);
    stubLongColumn(RANK_TABLE, "node_ref", NODE_REF);
    stubLongColumn(RANK_TABLE, "parent", PARENT);
    stubStringColumn(RANK_TABLE, "edge_type", EDGETYPE.getTypeChar());
    stubStringColumn(COMPONENT_TABLE, "namespace", EDGE_NAMESPACE);
    stubStringColumn(COMPONENT_TABLE, "name", EDGE_NAME);
  }

  private Edge createDefaultEdge()
  {
    Edge expected = new Edge();
    expected.setPre(PRE);
    expected.setEdgeType(EDGETYPE);
    expected.setNamespace(EDGE_NAMESPACE);
    expected.setName(EDGE_NAME);
    expected.setSource(new AnnisNode(PARENT));
    expected.setDestination(new AnnisNode(NODE_REF));
    return expected;
  }
  
  private void stubDefaultAnnotation(String table) throws SQLException 
  {
    stubStringColumn(table, "namespace", NAMESPACE);
    stubStringColumn(table, "name", NAME);
    stubStringColumn(table, "value", VALUE);
  }
  
  private void stubStringColumn(String table, final String column,
      final String value) throws SQLException
  {
    String prefix = uniqueString();
    given(tableAccessStrategy.columnName(table, column)).willReturn(
        prefix + column);
    given(resultSet.getString(prefix + column)).willReturn(value);
  }

  private void stubLongColumn(String table, final String column,
      final Long value) throws SQLException
  {
    String prefix = uniqueString();
    given(tableAccessStrategy.columnName(table, column)).willReturn(
        prefix + column);
    given(resultSet.getLong(prefix + column)).willReturn(value);
  }

}
