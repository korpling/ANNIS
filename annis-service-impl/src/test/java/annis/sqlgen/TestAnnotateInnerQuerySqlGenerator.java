package annis.sqlgen;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.test.TestUtils.newSet;
import static annis.test.TestUtils.uniqueAlphaString;
import static annis.test.TestUtils.uniqueInt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;

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
  @Mock private AnnisKey key;
  
  // test data
  @Mock private QueryData queryData;
  @Mock private AnnotateQueryData annotateQueryData;
  private List<QueryNode> alternative = new ArrayList<QueryNode>(); 
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
    given(annotateQueryData.getLeft()).willReturn(left);
    given(annotateQueryData.getRight()).willReturn(right);
    given(queryData.getExtensions()).willReturn(newSet((Object) annotateQueryData));
    String idAlias1 = uniqueAlphaString();
    String nameAlias1 = uniqueAlphaString();
    String idAlias2 = uniqueAlphaString();
    String nameAlias2 = uniqueAlphaString();
    given(key.generateInnerQueryColumns(tableAccessStrategy, 1)).willReturn(asList(idAlias1 + " AS " + "id1", nameAlias1 + " AS " + "name1"));
    given(key.generateInnerQueryColumns(tableAccessStrategy, 2)).willReturn(asList(idAlias2 + " AS " + "id2", nameAlias2 + " AS " + "name2"));
    String textRefAlias1 = uniqueAlphaString();
    String leftTokenAlias1 = uniqueAlphaString();
    String rightTokenAlias1 = uniqueAlphaString();
    String textRefAlias2 = uniqueAlphaString();
    String leftTokenAlias2 = uniqueAlphaString();
    String rightTokenAlias2 = uniqueAlphaString();
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")).willReturn(textRefAlias1, textRefAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token")).willReturn(leftTokenAlias1, leftTokenAlias2);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "right_token")).willReturn(rightTokenAlias1, rightTokenAlias2);
    // when
    String actual = generator.selectClause(queryData, alternative, INDENT);
    // then
    String expected = "DISTINCT" + "\n" + INDENT + TABSTOP + 
        idAlias1 + " AS " + "id" + 1 + ", " +
        nameAlias1  + " AS " + "name" + 1 + ", " + 
        textRefAlias1  + " AS " + "text" + 1 + ", " + 
        leftTokenAlias1  + " - " + left + " AS " + "min" + 1 + ", " + 
        rightTokenAlias1  + " + " + right + " AS " + "max" + 1 + ", " + 
        "\n" + INDENT + TABSTOP + 
        idAlias2 + " AS " + "id" + 2 + ", " +
        nameAlias2  + " AS " + "name" + 2 + ", " + 
        textRefAlias2  + " AS " + "text" + 2 + ", " + 
        leftTokenAlias2  + " - " + left + " AS " + "min" + 2 + ", " + 
        rightTokenAlias2  + " + " + right + " AS " + "max" + 2;
    assertThat(actual, is(expected));
  }
  
}
