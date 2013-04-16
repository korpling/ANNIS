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

  private VisualizationDefinition currentDefinition;
  
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
      // provoce an error
      tree.toStringTree();
    }
  }

  @Override
  public void enterVis(
    HTMLVisConfigParser.VisContext ctx)
  {
    currentDefinition = new VisualizationDefinition();
    // set to nothing by default, otherwise it will be overwritten later
    currentDefinition.setOutputType(VisualizationDefinition.OutputType.NOTHING);
  }

  @Override
  public void enterConditionNoValue(
    HTMLVisConfigParser.ConditionNoValueContext ctx)
  {
    currentDefinition.setMatchingElement(ctx.ID().getText());
    currentDefinition.setMatchingValue(null);
  }

  @Override
  public void enterConditionWithValue(
    HTMLVisConfigParser.ConditionWithValueContext ctx)
  {
    currentDefinition.setMatchingElement(ctx.ID().getText());
    currentDefinition.setMatchingValue(ctx.value().innervalue().getText());
  }

  @Override
  public void enterElementNoStyle(HTMLVisConfigParser.ElementNoStyleContext ctx)
  {
    currentDefinition.setOutputElement(ctx.ID().getText());
    currentDefinition.setStyle("");
  }

  @Override
  public void enterElementWithStyle(
    HTMLVisConfigParser.ElementWithStyleContext ctx)
  {
    currentDefinition.setOutputElement(ctx.ID().getText());
    currentDefinition.setStyle(ctx.value().innervalue().getText());
  }
  
  @Override
  public void exitVis(HTMLVisConfigParser.VisContext ctx)
  {
    if(currentDefinition != null)
    {
      definitions.add(currentDefinition);
    }
  }

  public VisualizationDefinition[] getDefinitions()
  {
    return definitions.toArray(new VisualizationDefinition[definitions.size()]);
  }
  
  
}
