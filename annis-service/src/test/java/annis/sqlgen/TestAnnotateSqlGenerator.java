package annis.sqlgen;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.SubgraphFilter;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;
public class TestAnnotateSqlGenerator
{
  
  private class DummyAnnotateSqlGenerator extends AnnotateSqlGenerator<Integer> {
    @Override
    public Integer extractData(ResultSet arg0) throws SQLException,
        DataAccessException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public String selectClause(QueryData queryData,
      List<QueryNode> alternative, String indent)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public String fromClause(QueryData queryData,
      List<QueryNode> alternative, String indent)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getDocumentQuery(long toplevelCorpusID,
      String documentName, List<String> nodeAnnoFilter)
    {
      throw new UnsupportedOperationException();
    }
  };
  
  // class under test
  private DummyAnnotateSqlGenerator generator = new DummyAnnotateSqlGenerator() {
    protected TableAccessStrategy createTableAccessStrategy() {
      return tableAccessStrategy;
    };
    protected SolutionKey<?> createSolutionKey() {
      return solutionKey;
    };
  };
  
  // dependencies
  @Mock private TableAccessStrategy tableAccessStrategy;
  @Mock private SolutionKey<?> solutionKey;
  
  // test data
  @Mock private QueryData queryData;
  @Mock private AnnotateQueryData annotateQueryData;
  @Mock private LimitOffsetQueryData limitOffsetQueryData;
  private List<QueryNode> alternative = new ArrayList<>(); 
  private static final String INDENT = TABSTOP;
  
  @Before
  public void setup()
  {
    initMocks(this);
    given(queryData.getExtensions(AnnotateQueryData.class)).willReturn(asList(annotateQueryData));
    setupOuterQueryFactsTableColumnAliases(generator);
  }
  
  public static void setupOuterQueryFactsTableColumnAliases(AnnotateExtractor<?> generator) {
    Map<String, String> nodeColumns = new HashMap<>();
    nodeColumns.put("namespace", "node_namespace");
    nodeColumns.put("name", "node_name");

    Map<String, String> nodeAnnotationColumns = new HashMap<>();
    nodeAnnotationColumns.put("node_ref", "id");
    nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
    nodeAnnotationColumns.put("name", "node_annotation_name");
    nodeAnnotationColumns.put("value", "node_annotation_value");

    Map<String, String> edgeAnnotationColumns = new HashMap<>();
    nodeAnnotationColumns.put("rank_ref", "pre");
    edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
    edgeAnnotationColumns.put("name", "edge_annotation_name");
    edgeAnnotationColumns.put("value", "edge_annotation_value");

    Map<String, String> edgeColumns = new HashMap<>();
    edgeColumns.put("node_ref", "id");
    edgeColumns.put("id", "rank_id");

    Map<String, String> componentColumns = new HashMap<>();
    componentColumns.put("id", "component_id");
    componentColumns.put("name", "edge_name");
    componentColumns.put("namespace", "edge_namespace");
    componentColumns.put("type", "edge_type");

    edgeColumns.put("name", "edge_name");
    edgeColumns.put("namespace", "edge_namespace");

    Map<String, Map<String, String>> columnAliases =
      new HashMap<>();
    columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
    columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE,
      nodeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE,
      edgeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);
    columnAliases.put(TableAccessStrategy.COMPONENT_TABLE, componentColumns);
    
    TableAccessStrategy outerQueryTableAccessStrategy = new TableAccessStrategy();
    outerQueryTableAccessStrategy.setColumnAliases(columnAliases);
    generator.setOuterQueryTableAccessStrategy(outerQueryTableAccessStrategy);
  }
  
  /**
   * It is the responsibility of the code that uses this class to make sure
   * that a fresh key management instance is generated when necessary.
   */
  @Test(expected=UnsupportedOperationException.class)
  public void shouldBailIfGetAnnisKeyMethodIsNotOverwritten()
  {
    // given
    generator = new DummyAnnotateSqlGenerator();
    // when
    generator.createSolutionKey();
  }
  
  /**
   * The result should be ordered by the solution key and the pre value of the node
   */
  @Test
  public void shouldOrderByKeyComponentAndPreValue()
  {
    // given
    String edgeNameAlias = createColumnAlias(COMPONENT_TABLE, "name");
    String preAlias = createColumnAlias(RANK_TABLE, "pre");
    String idAlias = createColumnAlias(COMPONENT_TABLE, "id");
    // when
    String actual = generator.orderByClause(queryData, alternative, INDENT);
    // then
    String expected = "solutions.n, " + edgeNameAlias + ", " + idAlias + ", " + preAlias;
    assertThat(actual, is(expected));
  }
  
  @Test
  public void shouldAddIsTokenOnFilter()
  {
    given(annotateQueryData.getFilter()).willReturn(SubgraphFilter.token);
    
    String isTokenAlias = createColumnAlias(NODE_TABLE, "is_token");
    
    String expected = isTokenAlias + " IS TRUE";
    
    Set<String> actualConditions = generator.whereConditions(queryData, alternative, "");
    assertTrue("WHERE conditions must include \"" + expected + "\"", actualConditions.contains(expected));
  }
  // set up a column alias of the form "table.column" 
  private String createColumnAlias(String table, String column)
  {
    String columnAlias = table + "." + column;
    given(tableAccessStrategy.aliasedColumn(table, column)).willReturn(columnAlias);
    return columnAlias;
  }
}
