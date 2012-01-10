package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.springframework.dao.DataAccessException;

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
  private AnnotateSqlGenerator generator;

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
    generator.createAnnisKey();
  }

}
