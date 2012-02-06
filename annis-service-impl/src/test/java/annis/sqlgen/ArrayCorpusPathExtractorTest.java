package annis.sqlgen;

import static annis.test.TestUtils.createJdbcArray;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

public class ArrayCorpusPathExtractorTest
{
  
  // class under test
  CorpusPathExtractor extractor = new ArrayCorpusPathExtractor();

  @Test
  public void shouldExtractCorpusPath() throws SQLException
  {
    // given
    String pathAlias = uniqueString(5);
    String path1 = uniqueString(3);
    String path2 = uniqueString(3);
    String path3 = uniqueString(3);
    ResultSet resultSet = mock(ResultSet.class);
    Array array = createJdbcArray(path1, path2, path3);
    given(resultSet.getArray(pathAlias)).willReturn(array);
    // when
    List<String> path = extractor.extractCorpusPath(resultSet, pathAlias);
    // then
    assertThat(path, is(asList(path3, path2, path1)));
  }
}
