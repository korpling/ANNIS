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

import annis.model.AnnisConstants;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SpanHTMLOutputter
{
  public enum Type {EMPTY, VALUE, ANNO_NAME, CONSTANT};
  
  private Type type = Type.EMPTY;
  private String element = "div";
  private String style = "";
  private String constant;
  
  public void outputHTML(SNode node, String matchedQName, Map<Long, List<String>> output)
  {
    // get left and right border
    long left=0;
    long right=0;
    if(node instanceof SSpan)
    {
      outputSpan((SSpan) node, matchedQName, output);
    }
    else if(node instanceof SToken)
    {
      SToken tok = (SToken) node;
      left = right = 
        tok.getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_TOKENINDEX).
        getSValueSNUMERIC();
    }
    else
    {
      throw new IllegalArgumentException("node must be either a SSpan or SToken");
    }
  }
  
  private void outputSpan(SSpan span, String matchedQName, Map<Long, List<String>> output)
  {
    long left = span
        .getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_LEFTTOKEN)
        .getSValueSNUMERIC();
      
    long right = span
        .getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RIGHTTOKEN)
        .getSValueSNUMERIC();
    
    String startTag = "<" + element;
    if(!style.isEmpty())
    {
      startTag += " style=\"" + style + "\" ";
    }
    startTag += ">";
    String inner = "";
    String endTag = "</" + element + ">";
    
    SAnnotation matchedAnnotation = span.getSAnnotation(matchedQName);
    
    switch(type)
    {
      case CONSTANT:
        inner = constant;
        break;
      case VALUE:
        inner = matchedAnnotation == null ? "NULL" : matchedAnnotation.getSValueSTEXT();
        break;
      case ANNO_NAME:
        inner = matchedAnnotation == null ? "NULL" : matchedAnnotation.getSName();
        break;
    }
    
    // add tags to output
    if(output.get(left) == null)
    {
      output.put(left, new ArrayList<String>());
    }
    if(output.get(right) == null)
    {
      output.put(right, new ArrayList<String>());
    }
    output.get(left).add(startTag + inner);
    output.get(right).add(0, endTag);
    
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  public String getElement()
  {
    return element;
  }

  public void setElement(String element)
  {
    this.element = element;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getConstant()
  {
    return constant;
  }

  public void setConstant(String constant)
  {
    this.constant = constant;
  }
  
}
