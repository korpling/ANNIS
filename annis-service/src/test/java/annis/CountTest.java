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

import annis.dao.QueryDao;
import annis.dao.QueryDaoImpl;
import annis.ql.parser.QueryData;
import annis.test.TestHelper;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
// TODO: do not test context only for annopool
@ContextConfiguration(locations =
{
  "file:src/main/distribution/conf/spring/Common.xml",
  "file:src/main/distribution/conf/spring/SqlGenerator.xml",
  "file:src/main/distribution/conf/spring/Dao.xml"
}, loader = AnnisXmlContextLoader.class)
public class CountTest
{

  Logger log = LoggerFactory.getLogger(CountTest.class);

  @Resource(name = "queryDao")
  QueryDao annisDao;

  private List<Long> pcc2CorpusID;

  private List<Long> tiger2CorpusID;

  @Before
  public void setup()
  {
    QueryDaoImpl springAnnisDao = (QueryDaoImpl) TestHelper.proxyTarget(
      annisDao);

    try
    {
      assumeNotNull(springAnnisDao.getJdbcTemplate());

      springAnnisDao.getJdbcTemplate().queryForInt("SELECT 1");

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
    List<String> corpusNames = new LinkedList<>();
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

    // test if precendence optimizatin is applied correctly when it is used with spans
    // that cover more than one token
    assertEquals(2, countPcc2("NP & NP & NP &  #1 . #2 & #2 . #3"));
    
    // test different annotations and component normalization in one query
    assertEquals(20, countPcc2("a#node "
      + "& (b#ambiguity | b#anaphor_type) "
      + "& c#node "
      + "& #a ->anaphor_antecedent #b "
      + "& #b ->anaphor_antecedent #c"));
    
    // test near operators
    assertEquals(2, countPcc2("pos=\"KON\" & pos=\"NN\" & #1 ^ #2"));
    assertEquals(5, countPcc2("pos=\"KON\" & pos=\"NN\" & #1 ^3 #2"));
    assertEquals(8, countPcc2("pos=\"KON\" & pos=\"NN\" & #1 ^3,4 #2"));
    assertEquals(184, countPcc2("pos=\"KON\" & pos=\"NN\" & #1 ^* #2")); //assuming indirect precendence bound is default (50)
    
    // regression tests:
    assertEquals(78, countPcc2("Inf-Stat & NP & #1 _=_ #2"));
    assertEquals(2, countPcc2("cat=\"CS\" >[func=\"CJ\"] cat=\"S\" > \"was\""));
    assertEquals(1, countPcc2("cat=\"CS\" >[func=\"CJ\"] cat=\"S\" >secedge \"was\""));
    assertEquals(1, countPcc2("pos=/V.FIN/ ->dep[func=\"sbj\"] \"Jugendliche\" & cat=\"S\" & #3 >secedge #2 | \"ja\""));
    assertEquals(326, countPcc2("pos!=/(NN)|(NE)/"));
    assertEquals(388, countPcc2("pos!=\"NE\""));
    assertEquals(187, countPcc2("tok & meta::Titel=\"Steilpass\""));
    assertEquals(212, countPcc2("tok & meta::Titel!=\"Steilpass\""));
    assertEquals(10, countPcc2("a#node "
      + "& b#ambiguity "
      + "& c#node "
      + "& #a ->anaphor_antecedent #b "
      + "& #b ->anaphor_antecedent #c"));
    
    assertEquals(2, countPcc2("\"wollen\" & tok & tok \n" +
"& #1 ->dep[func=\"obja\"] #2 \n" +
"& #1 ->dep[func=\"sbj\"] #3"));
    
    assertEquals(17, countPcc2("NP ^ PP "));
    assertEquals(118, countPcc2("NP ^1,10 PP "));
    assertEquals(477, countPcc2("NP ^* PP "));
    assertEquals(3, countPcc2("\"in\" ^ Inf-Stat=\"new\""));
    assertEquals(29, countPcc2("pos=\"NN\" ^ pos=\"ART\""));
    assertEquals(520, countPcc2("pos=\"NN\" ^* pos=\"ART\""));
    
    
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
      ".", ".*", ">", ">*", "_=_" , "_i_", "_o_", "_l_", "_r_", "->dep", "->dep *",
      ">@l", ">@r", "$", "$*", "^", "^*"
    };


    // get node and token count as reference
    int nodeCount = countPcc2("node");


    // automatic testing for "node"
    for (String op : operatorsToTest)
    {
      try
      {
        int nodeResult = countPcc2("node & node & #1 " + op + " #2");
        assertFalse("\"" + op + "\" operator should be non-reflexive for nodes",
          nodeCount == nodeResult);
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
    
    // manual testing
    assertEquals(0, countPcc2("pos=/.*/ & lemma=/.*/ & #1 _ident_ #2"));
  }

  @Test
  public void testReflexivityPcc2()
  {
    assumeTrue(pcc2CorpusID.size() > 0);

    // get token count as reference
    int tokenCount = countPcc2("tok");

    assertEquals(tokenCount, countPcc2("tok & tok & #1 _ident_ #2"));

    assertEquals(tokenCount, countPcc2("tok & pos=/.*/ & #1 _=_ #2"));
    assertEquals(tokenCount, countPcc2("pos=/.*/ & lemma=/.*/ & #1 _=_ #2"));
    
    // test that is is possible to search for the same node/token if the searched annotations are different
    assertEquals(1, countPcc2("\"Karola\" & pos=\"NE\" & #1 _l_ #2 & #1 _r_ #2"));
    assertEquals(1, countPcc2("lemma=\"Karola\" & pos=\"NE\" & #1 _l_ #2 & #1 _r_ #2"));
    assertEquals(2, countPcc2("a#\"Karola\" & (b#lemma=\"Karola\" | b#pos=\"NE\") " 
      + "& #a _=_ #b"));
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
