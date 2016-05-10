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
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class VisParser extends HTMLVisConfigBaseListener
{
  private List<VisualizationDefinition> definitions;;

  private SpanMatcher currentMatcher;
  private SpanHTMLOutputter currentOutputter;
  
  public VisParser(InputStream inStream) throws IOException, VisParserException
  {
    this.definitions = new LinkedList<VisualizationDefinition>();
    
    HTMLVisConfigLexer lexer = new HTMLVisConfigLexer(new ANTLRInputStream(inStream));
    HTMLVisConfigParser parser = new HTMLVisConfigParser(new CommonTokenStream(
      lexer));
    
    final List<String> errors = new LinkedList<String>();
    
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener()
    {

      @Override
      public void syntaxError(
        Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e)
      {
        errors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
      
    });
    
    ParseTree tree = parser.start();
    if(errors.isEmpty())
    {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk((VisParser) this, tree);
    }
    else
    {
      throw new VisParserException(
        "Parser error:\n" 
        + StringUtils.join(errors, "\n"));
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
    String namespace = null;
    if(ctx.qName().namespace != null)
    {
      namespace = ctx.qName().namespace.getText();
    }
    currentMatcher = new AnnotationNameMatcher(namespace, ctx.qName().name.getText());
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
  public void enterConditionBegin(HTMLVisConfigParser.ConditionBeginContext ctx)
  {
    currentMatcher = new PseudoRegionMatcher(PseudoRegionMatcher.PseudoRegion.BEGIN);
  }

  @Override
  public void enterConditionEnd(HTMLVisConfigParser.ConditionEndContext ctx)
  {
    currentMatcher = new PseudoRegionMatcher(PseudoRegionMatcher.PseudoRegion.END);
  }
  
  @Override
  public void enterConditionAll(HTMLVisConfigParser.ConditionAllContext ctx)
  {
    currentMatcher = new PseudoRegionMatcher(PseudoRegionMatcher.PseudoRegion.ALL);
  }

  @Override
  public void enterConditionNameAndValue(
    HTMLVisConfigParser.ConditionNameAndValueContext ctx)
  {
    String namespace = null;
    if(ctx.qName().namespace != null)
    {
      namespace = ctx.qName().namespace.getText();
    }
    currentMatcher = new AnnotationNameAndValueMatcher(namespace, ctx.qName().name.getText(), 
      ctx.value().innervalue().getText());
  }

  @Override
  public void enterElementNoStyle(HTMLVisConfigParser.ElementNoStyleContext ctx)
  {
    currentOutputter.setElement(ctx.ID().getText());
    currentOutputter.setStyle("");
  }

  @Override
  public void enterElementNoStyleAttribute(
    HTMLVisConfigParser.ElementNoStyleAttributeContext ctx)
  {
    currentOutputter.setElement(ctx.ID(0).getText());
    currentOutputter.setStyle("");
    currentOutputter.setAttribute(ctx.ID(1).getText());
  }
  
  

  @Override
  public void enterElementWithStyle(
    HTMLVisConfigParser.ElementWithStyleContext ctx)
  {
    currentOutputter.setElement(ctx.ID().getText());
    currentOutputter.setStyle(ctx.value().innervalue().getText());
  }

  @Override
  public void enterElementWithStyleAttribute(
    HTMLVisConfigParser.ElementWithStyleAttributeContext ctx)
  {
    currentOutputter.setElement(ctx.ID(0).getText());
    currentOutputter.setStyle(ctx.value().innervalue().getText());
    currentOutputter.setAttribute(ctx.ID(1).getText());
  }
  
  @Override
  public void enterTypeAnno(HTMLVisConfigParser.TypeAnnoContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.ANNO_NAME);
  }

    @Override
  public void enterTypeMeta(HTMLVisConfigParser.TypeMetaContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.META_NAME);
    //Using constant property for metadata, since constant and metavalue are never set simultaneously
    //Alternatively we could consider adding another property for meta, or renaming 'constant' to 
    //something more appropriate.
    currentOutputter.setMetaname(ctx.innermeta().getText().trim());
  }

  
  @Override
  public void enterTypeHtmlTemp(HTMLVisConfigParser.TypeHtmlTempContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.HTML_TEMPLATE);
    currentOutputter.setConstant(ctx.innerhtmltemp().getText());
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
  public void enterTypeEscapedValue(
    HTMLVisConfigParser.TypeEscapedValueContext ctx)
  {
    currentOutputter.setType(SpanHTMLOutputter.Type.ESCAPED_VALUE);
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
