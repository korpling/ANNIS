package annis.sqlgen;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.test.TestUtils.uniqueAlphaString;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import annis.administration.SchemeType;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;

public class TestAnnotateSqlGenerator
{
  
  private class DummyAnnotateSqlGenerator extends AnnotateSqlGenerator<Integer> {
    @Override
    public Integer extractData(ResultSet arg0) throws SQLException,
        DataAccessException
    {
      throw new NotImplementedException();
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
  private List<QueryNode> alternative = new ArrayList<QueryNode>(); 
  private static final String INDENT = TABSTOP;
  
  @Before
  public void setup()
  {
    initMocks(this);
    given(queryData.getExtensions(AnnotateQueryData.class)).willReturn(asList(annotateQueryData));
    setupOuterQueryFactsTableColumnAliases(generator);
  }
  
  public static void setupOuterQueryFactsTableColumnAliases(AnnotateSqlGenerator<?> generator) {
    Map<String, String> nodeColumns = new HashMap<String, String>();
    nodeColumns.put("namespace", "node_namespace");
    nodeColumns.put("name", "node_name");

    Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("node_ref", "id");
    nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
    nodeAnnotationColumns.put("name", "node_annotation_name");
    nodeAnnotationColumns.put("value", "node_annotation_value");

    Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("rank_ref", "pre");
    edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
    edgeAnnotationColumns.put("name", "edge_annotation_name");
    edgeAnnotationColumns.put("value", "edge_annotation_value");

    Map<String, String> edgeColumns = new HashMap<String, String>();
    edgeColumns.put("node_ref", "id");

    Map<String, String> componentColumns = new HashMap<String, String>();
    componentColumns.put("id", "component_id");
    componentColumns.put("name", "edge_name");
    componentColumns.put("namespace", "edge_namespace");
    componentColumns.put("type", "edge_type");

    edgeColumns.put("name", "edge_name");
    edgeColumns.put("namespace", "edge_namespace");

    Map<String, Map<String, String>> columnAliases =
      new HashMap<String, Map<String, String>>();
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
  @Test(expected=NotImplementedException.class)
  public void shouldBailIfGetAnnisKeyMethodIsNotOverwritten()
  {
    // given
    generator = new DummyAnnotateSqlGenerator();
    // when
    generator.createSolutionKey();
  }

  /**
   * The SELECT clause consists of the key columns, columns required to 
   * generate an Annis node and columns that contain the document hierarchy 
   * of the node.
   */
  @Test
  public void shouldGenerateSelectClauseAnnoPool()
  {
    shouldGenerateSelectClause(SchemeType.ANNOPOOL);
  }
  
  /**
   * The SELECT clause consists of the key columns, columns required to 
   * generate an Annis node and columns that contain the document hierarchy 
   * of the node.
   */
  @Test
  public void shouldGenerateSelectClauseFullFacts()
  {
    shouldGenerateSelectClause(SchemeType.FULLFACTS);
  }
    
  private void shouldGenerateSelectClause(SchemeType type)
  {
    generator.setTableLayout(type.name());
    
    // given
    int offset = uniqueInt(10);
    given(annotateQueryData.getOffset()).willReturn(offset);
    String keyColumn1 = uniqueAlphaString();
    String keyColumn2 = uniqueAlphaString();
    given(solutionKey.generateOuterQueryColumns(eq(tableAccessStrategy), anyInt())).willReturn(asList(keyColumn1, keyColumn2));
    String idAlias = createColumnAlias(NODE_TABLE, "id");
    String textRefAlias = createColumnAlias(NODE_TABLE, "text_ref");
    String corpusRefAlias = createColumnAlias(NODE_TABLE, "corpus_ref");
    String toplevelCorpusAlias = createColumnAlias(NODE_TABLE, "toplevel_corpus");
    String nodeNamespaceAlias = createColumnAlias(NODE_TABLE, "namespace");
    String nodeNameAlias = createColumnAlias(NODE_TABLE, "name");
    String leftAlias = createColumnAlias(NODE_TABLE, "left");
    String rightAlias = createColumnAlias(NODE_TABLE, "right");
    String tokenIndexAlias = createColumnAlias(NODE_TABLE, "token_index");
//    String isTokenAlias = createColumnAlias(NODE_TABLE, "is_token");
    String continuousAlias = createColumnAlias(NODE_TABLE, "continuous");
    String spanAlias = createColumnAlias(NODE_TABLE, "span");
    String leftTokenAlias = createColumnAlias(NODE_TABLE, "left_token");
    String rightTokenAlias = createColumnAlias(NODE_TABLE, "right_token");
    String preAlias = createColumnAlias(RANK_TABLE, "pre");
    String postAlias = createColumnAlias(RANK_TABLE, "post");
    String parentAlias = createColumnAlias(RANK_TABLE, "parent");
    String rootAlias = createColumnAlias(RANK_TABLE, "root");
    String levelAlias = createColumnAlias(RANK_TABLE, "level");
    String componentIdAlias = createColumnAlias(COMPONENT_TABLE, "id");
    String componentTypeAlias = createColumnAlias(COMPONENT_TABLE, "type");
    String componentNameAlias = createColumnAlias(COMPONENT_TABLE, "name");
    String componentNamespaceAlias = createColumnAlias(COMPONENT_TABLE, "namespace");
    String nodeAnnotatationNamespaceAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "namespace");
    String nodeAnnotatationNameAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "name");
    String nodeAnnotatationValueAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "value");
    String edgeAnnotationNamespaceAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "namespace");
    String edgeAnnotationNameAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "name");
    String edgeAnnotationValueAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "value");

    String pathNameAlias = createColumnAlias(CORPUS_TABLE, "path_name");
    // when
    String actual = generator.selectClause(queryData, alternative, INDENT);
    // then
    String expected = "DISTINCT" + "\n"
        + INDENT + TABSTOP + keyColumn1 + ",\n"
        + INDENT + TABSTOP + keyColumn2 + ",\n"
        + INDENT + TABSTOP + offset + " AS \"" + "matchstart" + "\",\n"
        + INDENT + TABSTOP + idAlias + " AS \"" + "id" + "\",\n"
        + INDENT + TABSTOP + textRefAlias + " AS \"" + "text_ref" + "\",\n"
        + INDENT + TABSTOP + corpusRefAlias + " AS \"" + "corpus_ref" + "\",\n"
        + INDENT + TABSTOP + toplevelCorpusAlias + " AS \"" + "toplevel_corpus" + "\",\n"
        + INDENT + TABSTOP + nodeNamespaceAlias + " AS \"" + "node_namespace" + "\",\n"
        + INDENT + TABSTOP + nodeNameAlias + " AS \"" + "node_name" + "\",\n"
        + INDENT + TABSTOP + leftAlias + " AS \"" + "left" + "\",\n"
        + INDENT + TABSTOP + rightAlias + " AS \"" + "right" + "\",\n"
        + INDENT + TABSTOP + tokenIndexAlias + " AS \"" + "token_index" + "\",\n"
        + INDENT + TABSTOP + continuousAlias + " AS \"" + "continuous" + "\",\n"
        + INDENT + TABSTOP + spanAlias + " AS \"" + "span" + "\",\n"
        + INDENT + TABSTOP + leftTokenAlias + " AS \"" + "left_token" + "\",\n"
        + INDENT + TABSTOP + rightTokenAlias + " AS \"" + "right_token" + "\",\n"
        + INDENT + TABSTOP + preAlias + " AS \"" + "pre" + "\",\n"
        + INDENT + TABSTOP + postAlias + " AS \"" + "post" + "\",\n"
        + INDENT + TABSTOP + parentAlias + " AS \"" + "parent" + "\",\n"
        + INDENT + TABSTOP + rootAlias + " AS \"" + "root" + "\",\n"
        + INDENT + TABSTOP + levelAlias + " AS \"" + "level" + "\",\n"
        + INDENT + TABSTOP + componentIdAlias + " AS \"" + "component_id" + "\",\n"
        + INDENT + TABSTOP + componentTypeAlias + " AS \"" + "edge_type" + "\",\n"
        + INDENT + TABSTOP + componentNameAlias + " AS \"" + "edge_name" + "\",\n"
        + INDENT + TABSTOP + componentNamespaceAlias + " AS \"" + "edge_namespace" + "\",\n";
        if (type == SchemeType.FULLFACTS)
        {
          expected = expected
            + INDENT + TABSTOP + nodeAnnotatationNamespaceAlias + " AS \"" + "node_annotation_namespace" + "\",\n"
            + INDENT + TABSTOP + nodeAnnotatationNameAlias + " AS \"" + "node_annotation_name" + "\",\n"
            + INDENT + TABSTOP + nodeAnnotatationValueAlias + " AS \"" + "node_annotation_value" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationNamespaceAlias + " AS \"" + "edge_annotation_namespace" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationNameAlias + " AS \"" + "edge_annotation_name" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationValueAlias + " AS \"" + "edge_annotation_value" + "\",\n";
        }
        else if(type == SchemeType.ANNOPOOL)
        {
          expected = expected
            + INDENT + TABSTOP + "node_anno.namespace AS \"node_annotation_namespace\",\n"
            + INDENT + TABSTOP + "node_anno.\"name\" AS \"node_annotation_name\",\n"
            + INDENT + TABSTOP + "node_anno.\"val\" AS \"node_annotation_value\",\n"
            + INDENT + TABSTOP + "edge_anno.namespace AS \"edge_annotation_namespace\",\n"
            + INDENT + TABSTOP + "edge_anno.\"name\" AS \"edge_annotation_name\",\n"
            + INDENT + TABSTOP + "edge_anno.\"val\" AS \"edge_annotation_value\",\n";
        }
        expected = expected + INDENT + TABSTOP + pathNameAlias + " AS " + "path";
    System.out.println("---> Actual");
    System.out.println(actual);
    System.out.println();
    System.out.println("---> Expected");
    System.out.println(expected);
    assertThat(actual, is(expected));
  }
  
  /**
   * The SELECT clause consists of the key columns, columns required to 
   * generate an Annis node and columns that contain the document hierarchy 
   * of the node.
   */
  @Test
  public void shouldGenerateSelectClauseWithIsTokenColumn()
  {
    // given
    SchemeType type = SchemeType.FULLFACTS;
    generator.setTableLayout(type.name());
    generator.setIncludeIsTokenColumn(true);
    int offset = uniqueInt(10);
    given(annotateQueryData.getOffset()).willReturn(offset);
    String keyColumn1 = uniqueAlphaString();
    String keyColumn2 = uniqueAlphaString();
    given(solutionKey.generateOuterQueryColumns(eq(tableAccessStrategy), anyInt())).willReturn(asList(keyColumn1, keyColumn2));
    String idAlias = createColumnAlias(NODE_TABLE, "id");
    String textRefAlias = createColumnAlias(NODE_TABLE, "text_ref");
    String corpusRefAlias = createColumnAlias(NODE_TABLE, "corpus_ref");
    String toplevelCorpusAlias = createColumnAlias(NODE_TABLE, "toplevel_corpus");
    String nodeNamespaceAlias = createColumnAlias(NODE_TABLE, "namespace");
    String nodeNameAlias = createColumnAlias(NODE_TABLE, "name");
    String leftAlias = createColumnAlias(NODE_TABLE, "left");
    String rightAlias = createColumnAlias(NODE_TABLE, "right");
    String tokenIndexAlias = createColumnAlias(NODE_TABLE, "token_index");
    String isTokenAlias = createColumnAlias(NODE_TABLE, "is_token");
    String continuousAlias = createColumnAlias(NODE_TABLE, "continuous");
    String spanAlias = createColumnAlias(NODE_TABLE, "span");
    String leftTokenAlias = createColumnAlias(NODE_TABLE, "left_token");
    String rightTokenAlias = createColumnAlias(NODE_TABLE, "right_token");
    String preAlias = createColumnAlias(RANK_TABLE, "pre");
    String postAlias = createColumnAlias(RANK_TABLE, "post");
    String parentAlias = createColumnAlias(RANK_TABLE, "parent");
    String rootAlias = createColumnAlias(RANK_TABLE, "root");
    String levelAlias = createColumnAlias(RANK_TABLE, "level");
    String componentIdAlias = createColumnAlias(COMPONENT_TABLE, "id");
    String componentTypeAlias = createColumnAlias(COMPONENT_TABLE, "type");
    String componentNameAlias = createColumnAlias(COMPONENT_TABLE, "name");
    String componentNamespaceAlias = createColumnAlias(COMPONENT_TABLE, "namespace");
    String nodeAnnotatationNamespaceAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "namespace");
    String nodeAnnotatationNameAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "name");
    String nodeAnnotatationValueAlias = createColumnAlias(NODE_ANNOTATION_TABLE, "value");
    String edgeAnnotationNamespaceAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "namespace");
    String edgeAnnotationNameAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "name");
    String edgeAnnotationValueAlias = createColumnAlias(EDGE_ANNOTATION_TABLE, "value");
    String pathNameAlias = createColumnAlias(CORPUS_TABLE, "path_name");
    // when
    String actual = generator.selectClause(queryData, alternative, INDENT);
    // then
    String expected = "DISTINCT" + "\n"
        + INDENT + TABSTOP + keyColumn1 + ",\n"
        + INDENT + TABSTOP + keyColumn2 + ",\n"
        + INDENT + TABSTOP + offset + " AS \"" + "matchstart" + "\",\n"
        + INDENT + TABSTOP + idAlias + " AS \"" + "id" + "\",\n"
        + INDENT + TABSTOP + textRefAlias + " AS \"" + "text_ref" + "\",\n"
        + INDENT + TABSTOP + corpusRefAlias + " AS \"" + "corpus_ref" + "\",\n"
        + INDENT + TABSTOP + toplevelCorpusAlias + " AS \"" + "toplevel_corpus" + "\",\n"
        + INDENT + TABSTOP + nodeNamespaceAlias + " AS \"" + "node_namespace" + "\",\n"
        + INDENT + TABSTOP + nodeNameAlias + " AS \"" + "node_name" + "\",\n"
        + INDENT + TABSTOP + leftAlias + " AS \"" + "left" + "\",\n"
        + INDENT + TABSTOP + rightAlias + " AS \"" + "right" + "\",\n"
        + INDENT + TABSTOP + tokenIndexAlias + " AS \"" + "token_index" + "\",\n"
        + INDENT + TABSTOP + isTokenAlias + " AS " + "\"is_token" + "\",\n"
        + INDENT + TABSTOP + continuousAlias + " AS \"" + "continuous" + "\",\n"
        + INDENT + TABSTOP + spanAlias + " AS \"" + "span" + "\",\n"
        + INDENT + TABSTOP + leftTokenAlias + " AS \"" + "left_token" + "\",\n"
        + INDENT + TABSTOP + rightTokenAlias + " AS \"" + "right_token" + "\",\n"
        + INDENT + TABSTOP + preAlias + " AS \"" + "pre" + "\",\n"
        + INDENT + TABSTOP + postAlias + " AS \"" + "post" + "\",\n"
        + INDENT + TABSTOP + parentAlias + " AS \"" + "parent" + "\",\n"
        + INDENT + TABSTOP + rootAlias + " AS \"" + "root" + "\",\n"
        + INDENT + TABSTOP + levelAlias + " AS \"" + "level" + "\",\n"
        + INDENT + TABSTOP + componentIdAlias + " AS \"" + "component_id" + "\",\n"
        + INDENT + TABSTOP + componentTypeAlias + " AS \"" + "edge_type" + "\",\n"
        + INDENT + TABSTOP + componentNameAlias + " AS \"" + "edge_name" + "\",\n"
        + INDENT + TABSTOP + componentNamespaceAlias + " AS \"" + "edge_namespace" + "\",\n";
        if (type == SchemeType.FULLFACTS)
        {
          expected = expected
            + INDENT + TABSTOP + nodeAnnotatationNamespaceAlias + " AS \"" + "node_annotation_namespace" + "\",\n"
            + INDENT + TABSTOP + nodeAnnotatationNameAlias + " AS \"" + "node_annotation_name" + "\",\n"
            + INDENT + TABSTOP + nodeAnnotatationValueAlias + " AS \"" + "node_annotation_value" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationNamespaceAlias + " AS \"" + "edge_annotation_namespace" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationNameAlias + " AS \"" + "edge_annotation_name" + "\",\n"
            + INDENT + TABSTOP + edgeAnnotationValueAlias + " AS \"" + "edge_annotation_value" + "\",\n";
        }
        else if(type == SchemeType.ANNOPOOL)
        {
          expected = expected
            + INDENT + TABSTOP + "node_anno.namespace AS \"node_annotation_namespace\",\n"
            + INDENT + TABSTOP + "node_anno.\"name\" AS \"node_annotation_name\",\n"
            + INDENT + TABSTOP + "node_anno.\"val\" AS \"node_annotation_value\",\n"
            + INDENT + TABSTOP + "edge_anno.namespace AS \"edge_annotation_namespace\",\n"
            + INDENT + TABSTOP + "edge_anno.\"name\" AS \"edge_annotation_name\",\n"
            + INDENT + TABSTOP + "edge_anno.\"val\" AS \"edge_annotation_value\",\n";
        }
        expected = expected + INDENT + TABSTOP + pathNameAlias + " AS " + "path";
    System.out.println("---> Actual");
    System.out.println(actual.replace("\t", "\\t").replace("\n", "\\n\n"));
    System.out.println("---> Expected");
    System.out.println(expected.replace("\t", "\\t").replace("\n", "\\n\n"));
    assertThat(actual, is(expected));
  }
  
  /**
   * The result should be ordered by the solution key and the pre value of the node
   */
  @Test
  public void shouldOrderByKeyAndPreValue()
  {
    // given
    String preAlias = createColumnAlias(RANK_TABLE, "pre");
    String keyAlias1 = uniqueString(3);
    String keyAlias2 = uniqueString(3);
    given(solutionKey.getKeyColumns(anyInt())).willReturn(asList(keyAlias1, keyAlias2));
    // when
    String actual = generator.orderByClause(queryData, alternative, INDENT);
    // then
    String expected = keyAlias1 + ", " + keyAlias2 + ", " + preAlias;
    assertThat(actual, is(expected));
  }

  // set up a column alias of the form "table.column" 
  private String createColumnAlias(String table, String column)
  {
    String columnAlias = table + "." + column;
    given(tableAccessStrategy.aliasedColumn(table, column)).willReturn(columnAlias);
    return columnAlias;
  }
}
