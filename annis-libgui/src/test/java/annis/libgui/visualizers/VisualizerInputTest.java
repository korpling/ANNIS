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

package annis.libgui.visualizers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class VisualizerInputTest
{
  
  public VisualizerInputTest()
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
  public void testSerializationOfSDocument() throws IOException, ClassNotFoundException
  {
    SaltProject project = SampleGenerator.createSaltProject();
    SDocument doc = project.getCorpusGraphs().get(0).getDocuments().get(0);
    
    VisualizerInput visInput = new VisualizerInput();
    visInput.setDocument(doc);
    visInput.setContextPath("anypath");
    
    File tmpFile = File.createTempFile("testSingeResultPanel", ".salt");
    FileOutputStream fOut = new FileOutputStream(tmpFile);
    try (ObjectOutputStream oOut = new ObjectOutputStream(fOut))
    {
      oOut.writeObject(visInput);
    }
    
    FileInputStream fIn = new FileInputStream(tmpFile);
    ObjectInputStream oIn = new ObjectInputStream(fIn);
    
    VisualizerInput restored = (VisualizerInput) oIn.readObject();
    
    // compare
    Assert.assertTrue(doc.equals(restored.getDocument()));
    Assert.assertEquals("anypath", restored.getContextPath());
  }
  
  
}
