/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component.htmlvis;

import annis.visualizers.htmlvis.VisParser;
import annis.visualizers.htmlvis.VisualizationDefinition;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ParserTest
{ 
  
  private static final Logger log = LoggerFactory.getLogger(ParserTest.class);
  
  public ParserTest()
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
  public void testBasicParsing()
  {
    try
    {
      InputStream inStream = ParserTest.class.getResourceAsStream("basicexample.config");
      VisParser parser = new VisParser(inStream);
      VisualizationDefinition[] definitions =  parser.getDefinitions();
      
      assertEquals("There must be 15 rules from the parsing", 15, definitions.length);
      
      assertEquals(definitions[0].getMatchingElement(), "title");
      assertEquals(definitions[0].getMatchingValue(), null);
      assertEquals(definitions[0].getOutputElement(), "b");
      assertEquals(definitions[0].getStyle(), "");
      
      assertEquals(definitions[1].getMatchingElement(), "chapter");
      assertEquals(definitions[1].getMatchingValue(), null);
      assertEquals(definitions[1].getOutputElement(), "p");
      assertEquals(definitions[1].getStyle(), "");
      
      assertEquals(definitions[2].getMatchingElement(), "chapter");
      assertEquals(definitions[2].getMatchingValue(), null);
      assertEquals(definitions[2].getOutputElement(), "i");
      assertEquals(definitions[2].getStyle(), "");
      
      assertEquals(definitions[3].getMatchingElement(), "chapter");
      assertEquals(definitions[3].getMatchingValue(), null);
      assertEquals(definitions[3].getOutputElement(), "i");
      assertEquals(definitions[3].getStyle(), "");
      
      // TODO: test all properties
      
    }
    catch (IOException ex)
    {
      log.error(null, ex);
      fail(ex.getLocalizedMessage());
    }
  }
}