/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis;

import annis.dao.AnnisDao;
import annis.dao.SpringAnnisDao;
import annis.ql.parser.QueryData;
import annis.test.TestHelper;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

;

/**
 * This will execute tests on a real database and check if the counts are OK.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
// TODO: do not test context only for annopool
@ContextConfiguration(locations =
{
  "file:src/main/distribution/conf/spring/Common.xml"
}, loader = AnnisXmlContextLoader.class)
public class CountTest
{

  Logger log = LoggerFactory.getLogger(CountTest.class);

  @Resource(name = "annisDao")
  AnnisDao annisDao;

  private List<Long> pcc2CorpusID;

  private List<Long> tiger2CorpusID;

  @Before
  public void setup()
  {
    SpringAnnisDao springAnnisDao = (SpringAnnisDao) TestHelper.proxyTarget(
      annisDao);

    try
    {
      assumeNotNull(springAnnisDao.getSimpleJdbcTemplate());

      springAnnisDao.getSimpleJdbcTemplate().queryForInt("SELECT 1");

    }
    catch (DataAccessException ex)
    {
      assumeNoException(ex);
    }

    // get the id of the "pcc2" corpus 
    pcc2CorpusID = getCorpusIDs("pcc2");

    // get the id of the "tiger2" corpus 
    tiger2CorpusID = getCorpusIDs("tiger2");
  }

  private List<Long> getCorpusIDs(String corpus)
  {
    // (and check if it's there, otherwise ignore these tests)
    List<String> corpusNames = new LinkedList<String>();
    corpusNames.add(corpus);
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(corpusNames);
    return corpusIDs;
  }

  @Test
  public void testAQLTestSuitePcc2()
  {
    assumeTrue(pcc2CorpusID.size() > 0);

    assertEquals(7, countPcc2("Topic=\"ab\" & Inf-Stat=/[Nn]ew/ & #1 _i_ #2"));
    assertEquals(5, countPcc2(
      "np_form=\"defnp\" & np_form=\"pper\"  & #2 ->anaphor_antecedent  #1"));
    assertEquals(13, countPcc2(
      "np_form=\"defnp\" & np_form=\"pper\"  & #2 ->anaphor_antecedent * #1"));
    assertEquals(2, countPcc2(
      "np_form=\"defnp\" & np_form=\"pper\"  & #2 ->anaphor_antecedent * #1 & cat=\"NP\" & cat=\"S\" & #4 >[func=\"SB\"] #3 & #3 _i_ #2"));
    assertEquals(3, countPcc2("Inf-Stat=\"new\" & PP & #1 _o_ #2"));
    assertEquals(1, countPcc2(
      "np_form=\"defnp\" & np_form=\"pper\"  & #2 ->anaphor_antecedent #1 & cat=\"NP\" & node & #4 >[func=\"OA\"] #3 & #3 _i_ #2"));

    assertEquals(5, countPcc2(
      "cat=\"NP\" & #1:arity=3 & node & #1 > #2 & #2:arity=3"));

    assertEquals(8, countPcc2("cat=\"S\" & tok & #1 >secedge #2\n"
      + "& cat=\"S\" \n"
      + "& #3 >* #2"));
    assertEquals(358, countPcc2("cat & cat & cat & #1 >* #2 & #2 >* #3"));
    assertEquals(10, countPcc2("cat & cat & tok & #1 >* #2 & #2 >secedge #3"));
    assertEquals(10, countPcc2(
      "node & node & node & #1 >edge * #2 & #2 >secedge * #3"));

  }

  @Test
  public void testAQLTestSuiteTiger2()
  {
    assumeTrue(tiger2CorpusID.size() > 0);

    assertEquals(11558, countTiger2(
      "cat=\"NP\" & cat=\"NP\" & #1 >[func=\"AG\"] #2"));
    assertEquals(13500, countTiger2("cat=\"NP\" & node & #1 >[func=\"AG\"] #2"));
    assertEquals(12328, countTiger2("cat=\"CS\" & cat=\"S\" & #1 > #2"));
    assertEquals(1029, countTiger2("pos=\"APPR\" & /.*ung/ & #1 . #2"));
    assertEquals(21, countTiger2(
      "pos=\"KOUS\" & tok=\"man\" & \"sich\" & #1 . #2 & #2 . #3"));
    assertEquals(3642, countTiger2(
      "cat=\"S\" & cat=\"PP\" & #1 >[func!=\"MO\"] #2"));
    assertEquals(22, countTiger2("/[Jj]e/ & \"desto\" & #1 .* #2"));
    assertEquals(5720, countTiger2("cat=\"S\" & cat=\"NP\" & #1 $ #2"));
    assertEquals(241, countTiger2(
      "pos=\"VVFIN\" & /[A-ZÖÜÄ].*/ & cat=\"S\" & #3 >@l #1 & #1 _=_ #2"));
    assertEquals(14806, countTiger2("cat=\"CS\" & cat=\"S\" & #1 >* #2"));

  }

  @Test
  public void testNonReflexivityPcc2()
  {

    assumeTrue(pcc2CorpusID.size() > 0);

    String[] operatorsToTest = new String[]
    {
      ".", ".*", ">", ">*", "_i_", "_o_", "_l_", "_r_", "->dep", "->dep *",
      ">@l", ">@r", "$", "$*"
    };


    // get token count as reference
    int tokenCount = countPcc2("tok");


    for (String op : operatorsToTest)
    {
      try
      {
        int tokResult = countPcc2("tok & tok & #1 " + op + " #2");
        assertFalse("\"" + op + "\" operator should be non-reflexive",
          tokenCount == tokResult);
      }
      catch (DataAccessException ex)
      {
        Throwable cause = ex.getCause();

        if (cause instanceof PSQLException)
        {
          int errorCode = ((PSQLException) cause).getErrorCode();
          if (errorCode == 0)
          {
            log.warn("test for op " + op + "aborted by timeout");
          }
          else
          {
            throw ex;
          }
        }
      }
    }
  }

  @Test
  public void testReflexivityPcc2()
  {
    assumeTrue(pcc2CorpusID.size() > 0);

    // get token count as reference
    int tokenCount = countPcc2("tok");

    assertEquals(tokenCount, countPcc2("tok & tok & #1 = #2"));
    assertEquals(tokenCount, countPcc2("pos=/.*/ & lemma=/.*/ & #1 = #2"));

    assertEquals(tokenCount, countPcc2("tok & tok & #1 _=_ #2"));
    assertEquals(tokenCount, countPcc2("pos=/.*/ & lemma=/.*/ & #1 _=_ #2"));
  }

  private int countPcc2(String aql)
  {
    System.out.println("pcc2 query: " + aql);
    return count(aql, pcc2CorpusID);
  }

  private int countTiger2(String aql)
  {
    System.out.println("tiger2 query: " + aql);
    return count(aql, tiger2CorpusID);
  }

  private int count(String aql, List<Long> corpora)
  {
    QueryData qd = annisDao.parseAQL(aql, corpora);
    return annisDao.count(qd);
  }
}
