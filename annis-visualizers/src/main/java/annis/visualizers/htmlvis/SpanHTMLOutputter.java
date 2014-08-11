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

import annis.CommonHelper;
import annis.model.AnnisConstants;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import annis.model.RelannisNodeFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SpanHTMLOutputter
{
    public enum Type {EMPTY, VALUE, ANNO_NAME, CONSTANT, META_NAME};
  
  public static final String NULL_VAL = "NULL";
  
  private Type type = Type.EMPTY;
  private String element = "div";
  private String attribute;
  private String style = "";
  private String constant;
  private String metaname;
  private HashMap<String, String> hshMeta = new HashMap<>();
  
  
  public void outputHTML(SNode node, String matchedQName,
    SortedMap<Long, SortedSet<OutputItem>> outputStartTags, 
    SortedMap<Long, SortedSet<OutputItem>> outputEndTags)
  {
    if(node instanceof SToken && "tok".equals(matchedQName))
    {
        SToken tok = (SToken) node;
        outputToken(tok, outputStartTags, outputEndTags);
    }
    else if(node instanceof SSpan || node instanceof SToken)
    {
        outputAnnotation(node, matchedQName, outputStartTags, outputEndTags);
    }
    else
    {
      throw new IllegalArgumentException("node must be either a SSpan or SToken");
    }
  }
  
  private void outputAnnotation(SNode span, String matchedQName, 
    SortedMap<Long, SortedSet<OutputItem>> outputStartTags, 
    SortedMap<Long, SortedSet<OutputItem>> outputEndTags)
  {
    long left;
    long right;
    
        RelannisNodeFeature feat = 
        (RelannisNodeFeature) span.getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        left = feat.getLeftToken();
        right = feat.getRightToken();
  
    SAnnotation matchedAnnotation;
    if (type == Type.META_NAME){
        matchedAnnotation = span.getSAnnotation("meta::" + constant); //constant property is used to store metadata names, see VisParser.java
    }
    else
    {
        matchedAnnotation = span.getSAnnotation(matchedQName);
    }
    
    String value;
    // output to an inner text node
    switch(type)
    {
      case CONSTANT:
        value = constant;
        break;
      case VALUE:
        value = matchedAnnotation == null ? "NULL" : matchedAnnotation.getSValueSTEXT();
        break;
      case ANNO_NAME:
        value = matchedAnnotation == null ? "NULL" : matchedAnnotation.getSName();
        break;
      case META_NAME:
        value = matchedAnnotation.getSValue() == null ? "NULL" : matchedAnnotation.getSValue().toString();
        matchedQName = "meta::" + metaname;
        break;
      default:
        value = "";
        break;
    }
    outputAny(left, right, matchedQName, value, outputStartTags, outputEndTags);
  }
  
  private void outputToken(SToken tok,
    SortedMap<Long, SortedSet<OutputItem>> outputStartTags, 
    SortedMap<Long, SortedSet<OutputItem>> outputEndTags)
  {

    RelannisNodeFeature feat = 
      (RelannisNodeFeature) tok.getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
    
    long index = feat.getTokenIndex();
    
    String value;
    
    switch(type)
    {
      case CONSTANT:
        value = constant;
        break;
      case VALUE:
        value = CommonHelper.getSpannedText(tok);
        break;
      case ANNO_NAME:
        value = "tok";
        break;
      default:
        value = "";
        break;
    }
    outputAny(index, index, "tok", value, outputStartTags, outputEndTags);    
  }
  
  public void outputAny(long left, long right, String matchedQName,
    String value, 
    SortedMap<Long, SortedSet<OutputItem>> outputStartTags, 
    SortedMap<Long, SortedSet<OutputItem>> outputEndTags)
  {
    
    String startTag = "<" + element;
    if(!style.isEmpty())
    {
      // is the style in reality a class name?
      if(style.contains(":") || style.contains(";"))
      {
        startTag += " style=\"" + style + "\" ";
      }
      else
      {
        startTag += " class=\"" + style + "\" ";
      }
    }
    String inner = "";
    String endTag = "</" + element + ">";
    
    if(attribute == null || attribute.isEmpty())
    {
      inner = value;
    }
    else
    {
      // output to an attribute
      startTag += " " + attribute + "=\"" + value + "\"";
    }
    
    startTag += ">";
    
    // add tags to output
    if(outputStartTags.get(left) == null)
    {
      outputStartTags.put(left, new TreeSet<OutputItem>());
    }
    if(outputEndTags.get(right) == null)
    {
      outputEndTags.put(right, new TreeSet<OutputItem>());
    }
    
    if(NULL_VAL.equals(element))
    {
      // reset both start and end tag since we won't use it
      startTag = "";
      endTag = "";
    }
    
    // <tag>|inner| ... | </tag>
    if(!inner.isEmpty())
    {
      startTag += inner;
    }
    
    OutputItem itemStart = new OutputItem();
    itemStart.setOutputString(startTag);
    itemStart.setLength(right-left);
    itemStart.setqName(matchedQName);
    
    OutputItem itemEnd = new OutputItem();
    if(endTag.isEmpty())
    {
      itemEnd.setOutputString(endTag+ "<!-- end of non-span output -->");
    }
    else
    {
      itemEnd.setOutputString(endTag + "<!-- end of \"" + style + "\" -->");
    }
    itemEnd.setLength(right-left);
    itemEnd.setqName(matchedQName);
    
    outputStartTags.get(left).add(itemStart);
    outputEndTags.get(right).add(itemEnd);

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

  public String getAttribute()
  {
    return attribute;
  }

  public void setAttribute(String attribute)
  {
    this.attribute = attribute;
  }

    public HashMap<String, String> getMeta() {
        return hshMeta;
    }

    public void setMeta( HashMap<String, String> meta) {
        this.hshMeta = meta;
    }

    public String getMetaname() {
        return metaname;
    }

    public void setMetaname(String metaname) {
        this.metaname = metaname;
    }
  
    
    
}

