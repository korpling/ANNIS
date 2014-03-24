/*
 * Copyright 2014 SFB 632.
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

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.samples.SampleGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CommonHelperTest
{
  
  public CommonHelperTest()
  {
  }
  
  @BeforeClass
  public static void setUpClass()
  {
  }
  
  @AfterClass
  public static void tearDownClass()
  {
  }
  
  @Before
  public void setUp()
  {
  }
  
  @After
  public void tearDown()
  {
  }

  @Test
  public void testEmbeddedSerializationOfSDocument() throws IOException, ClassNotFoundException
  {
    SaltProject project = SampleGenerator.createCompleteSaltproject();
    SDocument doc = project.getSCorpusGraphs().get(0).getSDocuments().get(0);
    
    
    File tmpFile = File.createTempFile("testSingeResultPanel", ".salt");
    FileOutputStream fOut = new FileOutputStream(tmpFile);
    ObjectOutputStream oOut = new ObjectOutputStream(fOut);
    
    int firstObject = 567;
    int lastObject = 1234;
    
    oOut.writeInt(firstObject);
    CommonHelper.writeSDocument(doc, oOut);
    oOut.writeInt(lastObject);
    oOut.close();
    
    FileInputStream fIn = new FileInputStream(tmpFile);
    ObjectInputStream oIn = new ObjectInputStream(fIn);
    
    Assert.assertEquals(firstObject, oIn.readInt());
    SDocument restored = CommonHelper.readSDocument(oIn);
    
    Assert.assertTrue(doc.equals(restored));
    
    Assert.assertEquals(lastObject, oIn.readInt());
  }
  
  
}
