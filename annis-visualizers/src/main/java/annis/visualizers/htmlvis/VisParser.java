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
package annis.visualizers.htmlvis;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VisParser extends HTMLVisConfigBaseListener
{
  private List<VisualizationDefinition> definitions;;

  private SpanMatcher currentMatcher;
  private SpanHTMLOutputter currentOutputter;
  
  public VisParser(InputStream inStream) throws IOException
  {
    this.definitions = new LinkedList<VisualizationDefinition>();
    
    HTMLVisConfigLexer lexer = new HTMLVisConfigLexer(new ANTLRInputStream(inStream));
    HTMLVisConfigParser parser = new HTMLVisConfigParser(new CommonTokenStream(
      lexer));
    
    ParseTree tree = parser.start();
    if(parser.getNumberOfSyntaxErrors() == 0)
    {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk((VisParser) this, tree);
    }
    else
    {
      // provoke an error
      tree.toStringTree();
    }
  }

  @Override
  public void enterVis(
    HTMLVisConfigParser.VisContext ctx)
  {
    currentMatcher = null;
    currentOutputter = new SpanHTMLOutputter();
    currentOutputter.setType(SpanHTMLOutputter.Type.EMPTY);
    currentOutputter.setElement("div");
  }

  @Override
  public void enterConditionName(HTMLVisConfigParser.ConditionNameContext ctx)
  {
    currentMatcher = new AnnotationNameMatcher(ctx.ID().getText());
  }

  @Override
  public void enterConditionTok(HTMLVisConfigParser.ConditionTokContext ctx)
  {
    currentMatcher = new TokenMatcher();
  }

  @Override
  public void enterConditionValue(HTMLVisConfigParser.ConditionValueContext ctx)
  {
    currentMatcher = new AnnotationValueMatcher(ctx.value().innervalue().getText());
  }

  @Override
  public void enterConditionNameAndValue(
    HTMLVisConfigParser.ConditionNameAndValueContext ctx)
  {
    currentMatcher = new AnnotationNameAndValueMatcher(ctx.ID().getText(), 
      ctx.value().innervalue().getText());
  }

  @Override
  public void enterElementNoStyle(HTMLVisConfigParser.ElementNoStyleContext ctx)
  {
    currentOutputter.setElement(ctx.ID().getText());
    currentOutputter.setStyle("");
  }

  @Override
  public void enterElementWithStyle(
    HTMLVisConfigParser.ElementWithStyleContext ctx)
  {
    currentOutputter.setElement(ctx.ID().getText());
    currentOutputter.setStyle(ctx.value().innervalue().getText());
  }
  

  @Override
  public void enterTypeAnno(HTMLVisConfigParser.TypeAnnoContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.ANNO_NAME);
  }

  @Override
  public void enterTypeConstant(HTMLVisConfigParser.TypeConstantContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.CONSTANT);
    currentOutputter.setConstant(ctx.innertype().getText());
  }

  @Override
  public void enterTypeValue(HTMLVisConfigParser.TypeValueContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.VALUE);
  }
  
  

  
  @Override
  public void exitVis(HTMLVisConfigParser.VisContext ctx)
  {
    if(currentMatcher != null && currentOutputter != null)
    {
      VisualizationDefinition def = new VisualizationDefinition();
      def.setMatcher(currentMatcher);
      def.setOutputter(currentOutputter);
      definitions.add(def);
    }
  }

  public VisualizationDefinition[] getDefinitions()
  {
    return definitions.toArray(new VisualizationDefinition[definitions.size()]);
  }
  
  
}
