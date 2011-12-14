/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import static annis.sqlgen.SqlConstraints.between;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.SqlConstraints.numberJoin;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.test.TestUtils.uniqueInt;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestSqlConstraints
{

  /**
   * Use BETWEEN SYMMETRIC for between predicates.
   */
  @Test
  public void shouldUseBetweenSymmetric()
  {
    // given
    SqlConstraints.setDisableBetweenPredicate(false);
    int min = uniqueInt();
    int max = uniqueInt();
    // when
    String actual = between("lhs", min, max);
    // then
    String expected = "lhs BETWEEN SYMMETRIC " + min + " AND " + max;
    assertEquals(expected, actual);
  }

  /**
   * Don't use BETWEEN SYMMETRIC (only use BETWEEN) when requested.
   */
  @Test
  public void shouldNotUseBetweenSymmetric()
  {
    // given
    SqlConstraints.setDisableBetweenPredicate(true);
    int min = uniqueInt();
    int max = uniqueInt();
    // when
    String actual = between("lhs", min, max);
    // then
    String expected = "lhs BETWEEN " + min + " AND " + max;
    assertEquals(expected, actual);
  }

  /**
   * Use BETWEEN SYMMETRIC for between predicates (explicit RHS version)
   */
  @Test
  public void shouldUseBetweenSymmetricRhs()
  {
    // given
    SqlConstraints.setDisableBetweenPredicate(false);
    int min = uniqueInt();
    int max = uniqueInt();
    // when
    String actual = between("lhs", "rhs", min, max);
    // then
    String expected = "lhs BETWEEN SYMMETRIC rhs + " + min + " AND rhs + "
        + max;
    assertEquals(expected, actual);
  }

  /**
   * Don't use BETWEEN SYMMETRIC (only use BETWEEN) when requested (explicit RHS
   * version).
   */
  @Test
  public void shouldNotUseBetweenSymmetricRhs()
  {
    // given
    SqlConstraints.setDisableBetweenPredicate(true);
    int min = uniqueInt();
    int max = uniqueInt();
    // when
    String actual = between("lhs", "rhs", min, max);
    // then
    String expected = "lhs BETWEEN rhs + " + min + " AND rhs + " + max;
    assertEquals(expected, actual);
  }

  /**
   * A positive offset is added to the right-hand side of a join.
   */
  @Test
  public void shouldAddPositiveOffsetToNumberJoinRhs()
  {
    String expected = "lhs <op> rhs + 0";
    String actual = numberJoin("<op>", "lhs", "rhs", 0);
    assertEquals(expected, actual);
  }

  /**
   * A negative offset is subtracted from the right-hand side of a join.
   */
  @Test
  public void shouldSubstractNegativeOffsetFromNumberJoinRhs()
  {
    String expected = "lhs <op> rhs - 1";
    String actual = numberJoin("<op>", "lhs", "rhs", -1);
    assertEquals(expected, actual);
  }

  /**
   * An SQL string is enclosed in single quotes (').
   */
  @Test
  public void shouldUseSingleQuotesForSqlString()
  {
    String string = "string";
    assertThat(sqlString(string), is("'" + string + "'"));
  }

  /**
   * The left-hand side and right-hand side are joined by an arbitrary infix
   * operation.
   */
  @Test
  public void shouldJoinLhsAndRhsWithOp()
  {
    String expected = "lhs <op> rhs";
    String actual = join("<op>", "lhs", "rhs");
    assertEquals(expected, actual);
  }

}
