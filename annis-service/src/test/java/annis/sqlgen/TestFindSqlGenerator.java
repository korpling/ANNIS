package annis.sqlgen;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public class TestFindSqlGenerator
{

  // class under test
  private FindSqlGenerator generator;

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
    generator = new FindSqlGenerator()
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
  public void shouldNotOptimizeDistinct()
  {
    // given
    generator.setOptimizeDistinct(false);
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(false);
    // when
    String actual = generator.selectClause(queryData, alternative, "");
    // then
    assertThat(actual, startsWith("DISTINCT"));
  }

  @Test
  public void shouldSkipDistinctIfOnlyNodeTablesAreUsed()
  {
    // given
    generator.setOptimizeDistinct(true);
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(false);
    // when
    String actual = generator.selectClause(queryData, alternative, "");
    // then
    assertThat(actual, not(startsWith("DISTINCT")));
  }

  @Test
  public void shouldUseDistinctIfEdgeTablesAreUsed()
  {
    // given
    generator.setOptimizeDistinct(true);
    setupQueryData();
    given(tableAccessStrategy.usesRankTable()).willReturn(true);
    // when
    String actual = generator.selectClause(queryData, alternative, "");
    // then
    assertThat(actual, startsWith("DISTINCT"));
  }

}
