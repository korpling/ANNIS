package annis.sqlgen;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestFindSqlGenerator
{

  // class under test
  private SolutionSqlGenerator generator;

  // test data
  @Mock
  private TableAccessStrategy tableAccessStrategy;
  @Mock
  private QueryData queryData;
  @Mock
  private QueryNode queryNode;
  private ArrayList<QueryNode> alternative = new ArrayList<>();

  @Before
  public void setup()
  {
    initMocks(this);
    generator = new SolutionSqlGenerator()
    {
      protected TableAccessStrategy createTableAccessStrategy()
      {
        return tableAccessStrategy;
      }
    };
  }

  private void setupQueryData()
  {
    alternative.add(queryNode);
    given(queryData.getMaxWidth()).willReturn(1);
  }


  @Test
  public void shouldSkipDistinctIfOnlyNodeTablesAreUsed()
  {
    // given
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(false);
    // when
    String actual = generator.selectClause(queryData, alternative, "");
    // then
    assertThat(actual, not(startsWith("DISTINCT")));
  }

  @Test
  public void shouldUseGroupByIfPartOfEdge()
  {
    // given
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(true);
    given(queryNode.isPartOfEdge()).willReturn(true);
    given(queryNode.isRoot()).willReturn(false);
    // when
    String actual = generator.groupByAttributes(queryData, alternative);
    // then
    Assert.assertNotNull(actual);
  }
  
  @Test
  public void shouldUseGroupByIfRoot()
  {
    // given
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(true);
    given(queryNode.isPartOfEdge()).willReturn(false);
    given(queryNode.isRoot()).willReturn(true);
    // when
    String actual = generator.groupByAttributes(queryData, alternative);
    // then
    Assert.assertNotNull(actual);
  }

}
