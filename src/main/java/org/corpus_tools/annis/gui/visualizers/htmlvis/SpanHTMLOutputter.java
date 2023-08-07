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
package org.corpus_tools.annis.gui.visualizers.htmlvis;


import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.corpus_tools.annis.gui.util.Helper;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class SpanHTMLOutputter {
  public enum Type {
    EMPTY, VALUE, ESCAPED_VALUE, ANNO_NAME, CONSTANT, META_NAME, HTML_TEMPLATE
  };

  public static final String NULL_VAL = "NULL";

  private final static Escaper htmlEscaper = HtmlEscapers.htmlEscaper();
  private Type type = Type.EMPTY;
  private String element = "div";
  private String attribute;
  private String style = "";
  private String constant;
  private String metaname;
  private HashMap<String, String> hshMeta = new HashMap<>();

  private String tokenColor;


  public String getAttribute() {
    return attribute;
  }

  public String getConstant() {
    return constant;
  }

  public String getElement() {
    return element;
  }

  public HashMap<String, String> getMeta() {
    return hshMeta;
  }

  public String getMetaname() {
    return metaname;
  }

  public String getStyle() {
    return style;
  }

  public Type getType() {
    return type;
  }

  private void outputAnnotation(SStructuredNode span, String matchedQName,
      SortedMap<Long, List<OutputItem>> outputStartTags,
      SortedMap<Long, List<OutputItem>> outputEndTags, int priority,
      Map<SToken, Long> token2index) {
    if (span.getGraph() instanceof SDocumentGraph) {
      SDocumentGraph graph = (SDocumentGraph) span.getGraph();


      List<SToken> coveredTokens = graph.getOverlappedTokens(span);
      if (coveredTokens.isEmpty()) {
        return;
      }

      coveredTokens = graph.getSortedTokenByText(coveredTokens);

      // use the left-most and right-most token indexes
      long left = token2index.get(coveredTokens.get(0));
      long right = token2index.get(coveredTokens.get(coveredTokens.size() - 1));

      SAnnotation matchedAnnotation;
      if (type == Type.META_NAME) {
        matchedAnnotation = span.getAnnotation("meta::" + constant); // constant property is used to
                                                                     // store metadata names, see
                                                                     // VisParser.java
      } else {
        matchedAnnotation = span.getAnnotation(matchedQName);
      }

      String value;
      // output to an inner text node
      switch (type) {
        case CONSTANT:
          value = constant;
          break;
        case HTML_TEMPLATE:
          String original = constant;
          String innerValue =
              matchedAnnotation == null ? "NULL" : matchedAnnotation.getValue_STEXT();
          String innerAnno = matchedAnnotation == null ? "NULL" : matchedAnnotation.getName();
          value = original.replaceAll("%%value%%", innerValue);
          value = value.replaceAll("%%anno%%", innerAnno);
          break;
        case VALUE:
          value = matchedAnnotation == null ? "NULL" : matchedAnnotation.getValue_STEXT();
          break;
        case ESCAPED_VALUE:
          value = htmlEscaper
              .escape(matchedAnnotation == null ? "NULL" : matchedAnnotation.getValue_STEXT());
          break;
        case ANNO_NAME:
          value = matchedAnnotation == null ? "NULL" : matchedAnnotation.getName();
          break;
        case META_NAME:
          value = matchedAnnotation.getValue() == null ? "NULL"
              : matchedAnnotation.getValue().toString();
          matchedQName = "meta::" + metaname;
          break;
        default:
          value = "";
          break;
      }
      outputAny(left, right, matchedQName, value, outputStartTags, outputEndTags, priority);
    }
  }

  public void outputAny(long left, long right, String matchedQName, String value,
      SortedMap<Long, List<OutputItem>> outputStartTags,
      SortedMap<Long, List<OutputItem>> outputEndTags, int priority) {

    String startTag = "<" + element;
    if (!style.isEmpty()) {
      // is the style in reality a class name?
      if (style.contains(":") || style.contains(";")) {
        startTag += " style=\"" + style + "\" ";
      } else {
        startTag += " class=\"" + style + "\" ";
        String colorStyle = " style=\" color:" + tokenColor + "\" ";
        startTag += colorStyle;
      }
    }
    String inner = "";
    String endTag = "</" + element + ">";

    if (attribute == null || attribute.isEmpty()) {
      inner = value;
    } else {
      // output to an attribute
      startTag += " " + attribute + "=\"" + value + "\"";
    }

    startTag += ">";

    // add tags to output
    if (outputStartTags.get(left) == null) {
      outputStartTags.put(left, new ArrayList<OutputItem>());
    }
    if (outputEndTags.get(right) == null) {
      outputEndTags.put(right, new ArrayList<OutputItem>());
    }

    if (NULL_VAL.equals(element)) {
      // reset both start and end tag since we won't use it
      startTag = "";
      endTag = "";
    }

    // <tag>|inner| ... | </tag>
    if (!inner.isEmpty()) {
      startTag += inner;
    }

    OutputItem itemStart = new OutputItem();
    itemStart.setOutputString(startTag);
    itemStart.setLength(right - left);
    itemStart.setqName(matchedQName);
    itemStart.setPriority(priority);

    OutputItem itemEnd = new OutputItem();
    if (endTag.isEmpty()) {
      itemEnd.setOutputString(endTag + "<!-- end of non-span output -->");
    } else {
      itemEnd.setOutputString(endTag + "<!-- end of \"" + style + "\" -->");
    }
    itemEnd.setLength(right - left);
    itemEnd.setqName(matchedQName);
    itemEnd.setPriority(priority);

    outputStartTags.get(left).add(itemStart);
    outputEndTags.get(right).add(itemEnd);

  }

  public void outputHTML(SStructuredNode node, String matchedQName,
      SortedMap<Long, List<OutputItem>> outputStartTags,
      SortedMap<Long, List<OutputItem>> outputEndTags, String tokenColor, int priority,
      Map<SToken, Long> token2index) {

    this.tokenColor = tokenColor;

    if (node instanceof SToken && "tok".equals(matchedQName)) {
      SToken tok = (SToken) node;
      outputToken(tok, outputStartTags, outputEndTags, priority, token2index);
    } else if (node instanceof SSpan || node instanceof SToken) {
      outputAnnotation(node, matchedQName, outputStartTags, outputEndTags, priority, token2index);
    } else {
      throw new IllegalArgumentException("node must be either a SSpan or SToken");
    }
  }

  private void outputToken(SToken tok, SortedMap<Long, List<OutputItem>> outputStartTags,
      SortedMap<Long, List<OutputItem>> outputEndTags, int priority,
      Map<SToken, Long> token2index) {
    long index = token2index.get(tok);

    String value;

    switch (type) {
      case CONSTANT:
        value = constant;
        break;
      case VALUE:
        value = Helper.getSpannedText(tok);
        break;
      case ESCAPED_VALUE:
        value = htmlEscaper.escape(Helper.getSpannedText(tok));
        break;
      case ANNO_NAME:
        value = "tok";
        break;
      default:
        value = "";
        break;
    }
    outputAny(index, index, "tok", value, outputStartTags, outputEndTags, priority);
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public void setConstant(String constant) {
    this.constant = constant;
  }

  public void setElement(String element) {
    this.element = element;
  }

  public void setMeta(HashMap<String, String> meta) {
    this.hshMeta = meta;
  }

  public void setMetaname(String metaname) {
    this.metaname = metaname;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void setType(Type type) {
    this.type = type;
  }



}

