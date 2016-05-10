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
import annis.provider.SaltProjectProvider;
import annis.test.TestHelper;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.google.common.io.ByteStreams;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import org.corpus_tools.salt.common.SaltProject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
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
@BenchmarkOptions(callgc = false, benchmarkRounds = 5, warmupRounds = 5)
@BenchmarkMethodChart(filePrefix = "annis-benchmark")
@BenchmarkHistoryChart(labelWith = LabelType.RUN_ID, maxRuns = 20)
@AxisRange(min = 0.0)
@Ignore
public class BenchmarkTest
{

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  @Resource(name = "queryDao")
  QueryDao annisDao;

  private List<Long> pcc2CorpusID;

  private List<Long> ridgesCorpusID;

  private final SaltProjectProvider provider = new SaltProjectProvider();

  private final OutputStream nullStream = ByteStreams.nullOutputStream();
  private final MediaType typeXMI = new MediaType("application", "xmi+xml");

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

    // get the id of the "Ridges_Herbology_Version_2.0" corpus
    ridgesCorpusID = getCorpusIDs("Ridges_Herbology_Version_2.0");


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
  public void mapSalt_Pcc4282()
  {
    assumeTrue(pcc2CorpusID.size() > 0);

    SaltProject p = annisDao.retrieveAnnotationGraph("pcc2",
        "4282", null);

    assertEquals(1, p.getCorpusGraphs().size());
  }

  @Test
  public void mapSaltAndSaveXMI_Pcc4282() throws IOException
  {
    assumeTrue(ridgesCorpusID.size() > 0);

    SaltProject p = annisDao.retrieveAnnotationGraph("pcc2",
        "4282", null);
    provider.writeTo(p, SaltProject.class, null, null,
      typeXMI, new StringKeyIgnoreCaseMultivaluedMap<>(),
      nullStream);
  }


  @Test
  public void mapSalt_SonderbaresKraeuterBuch()
  {
    assumeTrue(ridgesCorpusID.size() > 0);

    SaltProject p = annisDao.retrieveAnnotationGraph("Ridges_Herbology_Version_2.0",
        "sonderbares.kraeuterbuch.16175.11-21", null);

    assertEquals(1, p.getCorpusGraphs().size());
  }

  @Test
  public void mapSaltAndSaveXMI_SonderbaresKraeuterBuch() throws IOException
  {
    assumeTrue(ridgesCorpusID.size() > 0);

    SaltProject p = annisDao.retrieveAnnotationGraph("Ridges_Herbology_Version_2.0",
        "sonderbares.kraeuterbuch.16175.11-21", null);
    provider.writeTo(p, 
      SaltProject.class, null, null,
      typeXMI, null,
      nullStream);
  }
}
