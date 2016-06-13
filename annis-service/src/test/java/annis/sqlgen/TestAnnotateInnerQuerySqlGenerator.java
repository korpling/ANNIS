package annis.sqlgen;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.extensions.AnnotateQueryData;
import static annis.test.TestUtils.uniqueAlphaString;
import static annis.test.TestUtils.uniqueInt;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestAnnotateInnerQuerySqlGenerator
{

  // class under test
  @InjectMocks private AnnotateInnerQuerySqlGenerator generator = new AnnotateInnerQuerySqlGenerator() {
    @Override
    protected TableAccessStrategy createTableAccessStrategy()
    {
      return tableAccessStrategy;
    }
  };

  // dependencies
  @Mock private TableAccessStrategy tableAccessStrategy; 
  @Mock private SolutionKey key;
  
  // test data
  @Mock private QueryData queryData;
  @Mock private AnnotateQueryData annotateQueryData;
  private List<QueryNode> alternative = new ArrayList<>(); 
  private static final String INDENT = TABSTOP;
  
  @Before
  public void setup()
  {
    initMocks(this);
  }
  
  /**
   * The SELECT clause consists of the column text_ref, left, right and the 
   * key columns for each node in the query alternative. Left and right are
   * modified by the requested annotation context.
   */
  @Test
  public void shouldGenerateSelectClause()
  {
    // given
    alternative = Collections.nCopies(2, new QueryNode());
    int left = uniqueInt(10);
    int right = uniqueInt(20);
    
    List<AnnotateQueryData> extensions = new ArrayList<>();
    extensions.add(annotateQueryData);

    given(annotateQueryData.getLeft()).willReturn(left);
    given(annotateQueryData.getRight()).willReturn(right);
    given(queryData.getExtensions(AnnotateQueryData.class)).willReturn(
      extensions);    
    given(queryData.getMaxWidth()).willReturn(alternative.size());
    
    String key1Column1 = uniqueAlphaString();
    String key1Column2 = uniqueAlphaString();
    String key2Column1 = uniqueAlphaString();
    String key2Column2 = uniqueAlphaString();
    given(key.generateInnerQueryColumns(tableAccessStrategy, 1)).willReturn(asList(key1Column1, key1Column2));
    given(key.generateInnerQueryColumns(tableAccessStrategy, 2)).willReturn(asList(key2Column1, key2Column2));
    String textRefAlias1 = uniqueAlphaString();
    String leftTokenAlias1 = uniqueAlphaString();
    String rightTokenAlias1 = uniqueAlphaString();
    String textRefAlias2 = uniqueAlphaString();
    String leftTokenAlias2 = uniqueAlphaString();
    String rightTokenAlias2 = uniqueAlphaString();
    String corpusRefAlias1 = uniqueAlphaString();
    String corpusRefAlias2 = uniqueAlphaString();
    String nodeNameAlias1 = uniqueAlphaString();
    String nodeNameAlias2 = uniqueAlphaString();
    
    
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")).willReturn(textRefAlias1, textRefAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token")).willReturn(leftTokenAlias1, leftTokenAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "right_token")).willReturn(rightTokenAlias1, rightTokenAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "corpus_ref")).willReturn(corpusRefAlias1, corpusRefAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name")).willReturn(nodeNameAlias1, nodeNameAlias2);
    // when
    String actual = generator.selectClause(queryData, alternative, INDENT);
    // then
    String expected = "DISTINCT" + "\n" + INDENT + TABSTOP + 
        key1Column1 + ", " +
        key1Column2 + ", " + 
        textRefAlias1  + " AS " + "text" + 1 + ", " + 
        leftTokenAlias1  + " - " + left + " AS " + "min" + 1 + ", " + 
        rightTokenAlias1  + " + " + right + " AS " + "max" + 1 + ", " + 
        corpusRefAlias1 + " AS corpus1, " +
        nodeNameAlias1 + " AS name1, " +
        "\n" + INDENT + TABSTOP + 
        key2Column1 + ", " +
        key2Column2 + ", " + 
        textRefAlias2  + " AS " + "text" + 2 + ", " + 
        leftTokenAlias2  + " - " + left + " AS " + "min" + 2 + ", " + 
        rightTokenAlias2  + " + " + right + " AS " + "max" + 2 + ", " +
        corpusRefAlias2 + " AS corpus2, " +
        nodeNameAlias2 + " AS name2";
        
    assertThat(actual, is(expected));
  }
  
}
