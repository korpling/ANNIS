package annis.sqlgen;

import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TestAnnisKey
{

  // class under test
  private AnnisKey key = new AnnisKey();
  @Mock private TableAccessStrategy tableAccessStrategy;

  @Before
  public void setup()
  {
    initMocks(this);
  }

  @Test
  public void shouldGenerateColumnsForInnerQuery()
  {
    // given
    String idAlias = uniqueString(3);
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id")).willReturn(idAlias);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name")).willReturn(nameAlias);
    int index = uniqueInt(1, 10);
    // when
    List<String> actual = key.generateInnerQueryColumns(tableAccessStrategy, index);
    // then
    List<String> expected = asList(
        idAlias + index + " AS id" + index,
        nameAlias + index + " AS name" + index);
    assertThat(actual, is(expected));
  }

}
