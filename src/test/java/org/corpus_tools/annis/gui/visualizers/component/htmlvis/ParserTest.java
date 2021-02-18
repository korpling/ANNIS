/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.component.htmlvis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import org.corpus_tools.annis.gui.visualizers.htmlvis.AnnotationNameAndValueMatcher;
import org.corpus_tools.annis.gui.visualizers.htmlvis.AnnotationNameMatcher;
import org.corpus_tools.annis.gui.visualizers.htmlvis.AnnotationValueMatcher;
import org.corpus_tools.annis.gui.visualizers.htmlvis.SpanHTMLOutputter;
import org.corpus_tools.annis.gui.visualizers.htmlvis.TokenMatcher;
import org.corpus_tools.annis.gui.visualizers.htmlvis.VisParser;
import org.corpus_tools.annis.gui.visualizers.htmlvis.VisParserException;
import org.corpus_tools.annis.gui.visualizers.htmlvis.VisualizationDefinition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ParserTest
{ 
  
  private static final Logger log = LoggerFactory.getLogger(ParserTest.class);
  
  public ParserTest()
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
      
      assertEquals(17, definitions.length, "There must be 17 rules from the parsing");
      
      VisualizationDefinition def = definitions[0];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("title",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("b", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[1];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("chapter",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("p", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[2];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("chapter",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("i", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.CONSTANT, def.getOutputter().getType());
      assertEquals("Kapitel: ", def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[3];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("chapter",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("i", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[4];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("pb",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("color: grey", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.CONSTANT, def.getOutputter().getType());
      assertEquals("page ", def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[5];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("pb_n",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("color: grey", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[6];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("pb",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("table", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[7];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("pb",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("tr", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[8];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("column",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("td", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[9];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("column",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("colStyle", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[10];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("lb",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("p", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[11];
      assertTrue(def.getMatcher()instanceof AnnotationNameAndValueMatcher);
      assertEquals("lb_rend",((AnnotationNameAndValueMatcher) def.getMatcher()).getNameMatcher().getAnnotationName());
      assertEquals("italics",((AnnotationNameAndValueMatcher) def.getMatcher()).getAnnotationValue());
      assertEquals("i", def.getOutputter().getElement());
      assertEquals("", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[12];
      assertTrue(def.getMatcher()instanceof AnnotationValueMatcher);
      assertEquals("Gott",((AnnotationValueMatcher) def.getMatcher()).getAnnotationValue());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("color: red", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[13];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("speaker",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("color: grey", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.ANNO_NAME, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[14];
      assertTrue(def.getMatcher()instanceof TokenMatcher);
      assertEquals("span", def.getOutputter().getElement());
      assertEquals("tokStyle", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.EMPTY, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
      
      def = definitions[15];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("test",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("p", def.getOutputter().getElement());
      assertEquals("tokStyle", def.getOutputter().getStyle());
      assertEquals(SpanHTMLOutputter.Type.VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertEquals("title", def.getOutputter().getAttribute());
      
      def = definitions[16];
      assertTrue(def.getMatcher()instanceof AnnotationNameMatcher);
      assertEquals("pb_n",((AnnotationNameMatcher) def.getMatcher()).getAnnotationName());
      assertEquals("span", def.getOutputter().getElement());
      assertEquals(SpanHTMLOutputter.Type.ESCAPED_VALUE, def.getOutputter().getType());
      assertNull(def.getOutputter().getConstant());
      assertNull(def.getOutputter().getAttribute());
    }
    catch (IOException ex)
    {
      log.error(null, ex);
      fail(ex.getLocalizedMessage());
    }
    catch (VisParserException ex)
    {
      log.error(null, ex);
      fail(ex.getLocalizedMessage());
    }
  }
}