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
package annis.administration;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;


public class TestCorpusAdministration
{

  @Mock
  private AdministrationDao administrationDao;
  private CorpusAdministration administration;

  @Before
  public void setup()
  {
    initMocks(this);

    administration = new CorpusAdministration();
    administration.setAdministrationDao(administrationDao);
    
    Mockito.when(administrationDao.importCorpus(Matchers.anyString(), Matchers.
      anyString(), Matchers.anyBoolean(), Matchers.anyBoolean())).thenReturn(
        Boolean.TRUE);
  }

  @Test
  public void importCorporaOne() throws IOException
  {
    File f = createDummyANNIS("somePath", "corpus1");
    
    String path = f.getAbsolutePath();
    
    administration.importCorporaSave(true, null, null, false, path);

    // insertion of a corpus needs to follow an exact order
    InOrder inOrder = inOrder(administrationDao);

    verifyPreImport(inOrder);

    // verify that the corpus was imported
    verifyImport(inOrder, path);

    verifyPostImport(inOrder);

    inOrder.verify(administrationDao).analyzeParentFacts();
    
    // that should be it
    verifyNoMoreInteractions(administrationDao);
  }

  @Test
  public void importCorporaMany() throws IOException
  {
    File f1 = createDummyANNIS("somePath", "corpus1");
    File f2 = createDummyANNIS("anotherPath", "corpus2");
    File f3 = createDummyANNIS("yetAnotherPath", "corpus3");
    
    String path1 = f1.getPath();
    String path2 = f2.getPath();
    String path3 = f3.getPath();
    
    administration.importCorporaSave(true, null, null, false, path1, path2, path3);

    // insertion of a corpus needs to follow an exact order
    InOrder inOrder = inOrder(administrationDao);

    // drop indexes only once
    verifyPreImport(inOrder);

    // verify that each corpus was inserted in order
    verifyImport(inOrder, path1);
    verifyImport(inOrder, path2);
    verifyImport(inOrder, path3);

    inOrder.verify(administrationDao).analyzeParentFacts();
    
    // that should be it
    verifyNoMoreInteractions(administrationDao);
  }
  
  private File createDummyANNIS(String path, String corpusName) throws IOException
  {
    File tmp = Files.createTempDir();
    tmp.deleteOnExit();
    File root = new File(tmp, path);
    root.mkdirs();
    
    Files.append("0\t" + corpusName  + "\tCORPUS\tNULL\t0\t1", 
      new File(root, "corpus.annis"), Charsets.UTF_8);
    
    return root;
  }

  private void verifyPreImport(InOrder inOrder)
  {
    // no pre import actions yet
  }

  private void verifyPostImport(InOrder inOrder)
  {
    // no post import actions yet
  }

  // a correct import requires this order
  private void verifyImport(InOrder inOrder, String path)
  {
    inOrder.verify(administrationDao).importCorpus(path, null, true, false);
//    // create the staging area
//    inOrder.verify(administrationDao).createStagingArea(true);
//
//    // bulk import the data
//    inOrder.verify(administrationDao).bulkImport(path);
//
//    // compute and verify top-level corpus
//    inOrder.verify(administrationDao).computeTopLevelCorpus();
//
//    // update IDs in staging area
//    long corpusID = inOrder.verify(administrationDao).updateIds();
//
//    // import binaries
//    inOrder.verify(administrationDao).importBinaryData(path);
//
//    inOrder.verify(administrationDao).createStagingAreaIndexes();
//    inOrder.verify(administrationDao).analyzeStagingTables();
//
//
//    // post-process the data to speed up queries
//    inOrder.verify(administrationDao).computeLeftTokenRightToken();
//    inOrder.verify(administrationDao).computeRealRoot();
//    inOrder.verify(administrationDao).computeLevel();
//
//    // gather statistics about this corpus
//    inOrder.verify(administrationDao).computeCorpusStatistics();
//
//    inOrder.verify(administrationDao).updateCorpusStatsId(corpusID);
//
//    // apply constraints to ensure data integrity
//    inOrder.verify(administrationDao).applyConstraints();
//
//    inOrder.verify(administrationDao).analyzeStagingTables();
//
//    // insert the corpus from the staging area to the main db
//    inOrder.verify(administrationDao).insertCorpus();
//
//    inOrder.verify(administrationDao).computeCorpusPath(corpusID);
//
//    inOrder.verify(administrationDao).createAnnotations(corpusID);
//
//    // the facts child table must be created
//
//    inOrder.verify(administrationDao).createFacts(corpusID);
//
//    inOrder.verify(administrationDao).updateCorpusStatistic();
//
//    // drop the staging area is not necessary, because we have no staging area in this test
//    inOrder.verify(administrationDao).dropStagingArea();
//
//    // analyze facts table
//    inOrder.verify(administrationDao).analyzeFacts(corpusID);

  }
}
