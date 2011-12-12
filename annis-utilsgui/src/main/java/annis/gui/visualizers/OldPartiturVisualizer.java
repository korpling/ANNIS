/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.visualizers;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class OldPartiturVisualizer extends WriterVisualizer
{

  @Override
  public String getShortName()
  {
    return "old_grid";
  }

  
  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {
      //Converting paulaInline to Partitur Table
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      Document document = builder.parse(new InputSource(new StringReader(input.getPaula())));
      PaulaInline2PartiturWriter paula2Partitur = new PaulaInline2PartiturWriter(document);

      Set<String> namespaceSet = new HashSet<String>();
      namespaceSet.add(input.getNamespace());

      paula2Partitur.setNamespaceSet(namespaceSet);

      PaulaInline2PartiturWriter.Partitur partitur = paula2Partitur.getPartitur();


//START

      Map<String, StringBuffer> levelMarkup = new HashMap<String, StringBuffer>();
      Map<String, Long> levelCount = new HashMap<String, Long>();
      Map<String, Long> levelLength = new HashMap<String, Long>();

      Set<String> seenLevels = new HashSet<String>();

      List<PaulaInline2PartiturWriter.Token> tokenList = partitur.getTokenList();
      Map<String, Long> levelOffset = new HashMap<String, Long>();
      Map<String, PaulaInline2PartiturWriter.SpanAnnotation> lastSpan = new HashMap<String, PaulaInline2PartiturWriter.SpanAnnotation>();

      for(PaulaInline2PartiturWriter.Token token : tokenList)
      {
        seenLevels.clear();
        try
        {

          for(PaulaInline2PartiturWriter.SpanAnnotation span : partitur.get(token.getId()))
          {
            if(true || span.getName() != null)
            {
              //System.writer.append(span.getId() + ": " + span.getName() + " (" + span.getOffset() + " -> " + span.getLength() + "): " + span.getValue());
              String levelName = span.getName();
              StringBuffer markup = levelMarkup.get(levelName);
              if(markup == null)
              {
                markup = new StringBuffer();
                levelMarkup.put(levelName, markup);
              }

              Long currentOffset = levelOffset.get(levelName);

              //start a new row if this span start at the same position
              //as the previous span

              long localOffset = span.getOffset();

              //start new row if there is an overlap
              if(span.overlap(lastSpan.get(levelName)))
              {
                markup.append("\t<tr>\n\t\t");
                currentOffset = null;
              }
              else
              {
                if(markup.length() > 5 && "</tr>".equals(markup.substring(markup.length() - 5)))
                {
                  markup = markup.replace(markup.length() - 5, markup.length(), "");
                }
                //System.writer.append(markup);
              }
              lastSpan.put(levelName, span);

              if(currentOffset == null)
              {
                currentOffset = span.getOffset();
              }
              else
              {
                localOffset = span.getOffset() - currentOffset;
              }

              levelOffset.put(levelName, currentOffset + localOffset + span.getLength());

              if(localOffset > 0)
              {
                markup.append("<td colspan=\"" + localOffset + "\"></td>");
              }
              StringBuffer tokenIdsArray = new StringBuffer();
              int tokenCount = 0;
              for(String tokenId : span.getTokenIdSet())
              {
                tokenIdsArray.append((tokenCount++ > 0 ? "," : "") + tokenId);
              }
              String color = input.getMarkableMap()
                .containsKey(Long.toString(span.getId())) ? input.getMarkableMap().get(Long.toString(span.getId())) : "black";
              markup.append("<td colspan=\"" + span.getLength() + "\" class=\"single_event\" style=\"width: auto;\" annis:tokenIds=\"" + tokenIdsArray + "\" onMouseOver=\"toggleAnnotation(this, true);\" onMouseOut=\"toggleAnnotation(this, false);\"><div style=\"display: none;\">(id: " + span.getId() + ", token: " + span.getTokenId() + ", length: " + span.getLength() + ", offset: " + span.getOffset() + ") " + levelName + "</div><table style=\"width: 100%;\">");
              for(Entry<String, String> entry : span.entrySet())
              {
                markup.append("<tr><td title=\" - " + entry.getKey() + " = " + entry.getValue() + "\" style=\"width: 100%; color: " + color + ";\">" + entry.getValue() + "</td></tr>");
              }
              markup.append("</table></td></tr>");



              levelLength.put(levelName, span.getLength());

              try
              {
                levelCount.put(levelName, levelCount.get(levelName));
              }
              catch(NullPointerException e)
              {
                levelCount.put(levelName, 1l);
              }
            }
          }
        }
        catch(NullPointerException e)
        {
          e.printStackTrace();
        }
        writer.append("\n");
      }
      writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"); 
      
      writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" 
        + input.getResourcePath("old_grid/jbar.css") + "\" />");
      writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" 
        + input.getResourcePath("old_grid/jquery.tooltip.css") + "\" />");      
      writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" 
        + input.getResourcePath("old_grid/partitur.css") + "\" />");
      
      writer.append("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("old_grid/jquery-1.6.2.min.js") + "\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("old_grid/jquery.jbar.js") + "\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("old_grid/jquery.tooltip.min.js") + "\"></script>");
      
      writer.append("</head><body>");
      writer.append("<ul id=\"toolbar\"></ul>\n");
      writer.append("<div id=\"partiture\">\n");
      writer.append("<table class=\"partitur_table\">\n");


      List<String> levelNameList = new ArrayList<String>();
      for(Entry<String, StringBuffer> entry : levelMarkup.entrySet())
      {
        String levelName = entry.getKey().replaceAll("^.*?[.:]", "");
        writer.append("\t<tr id=\"level_" + levelName + "\"><td><i>" + levelName + "</i></td>\n\t\t");
        writer.append(entry.getValue() + "");
        if(!levelNameList.contains(levelName))
        {
          levelNameList.add(levelName);
        }
      }
      writer.append("\t<tr><td></td>\n\t\t");
      for(PaulaInline2PartiturWriter.Token token : tokenList)
      {
        String color = input.getMarkableMap().containsKey(Long.toString(token.getId())) ? input.getMarkableMap().get(Long.toString(token.getId())) : "black";
        writer.append("<td id=\"token_" + token.getId() + "\" style=\"font-weight: bold; width: auto; color: " + color + ";\">" + token.getText() + "</td>");
      }
      writer.append("\n\t</tr>\n");
      writer.append("</table></div>");
      writer.append("<script>\nvar levelNames = [");
      int i = 0;
      for(String levelName : levelNameList)
      {
        writer.append((i++ > 0 ? ", " : "") + "\"" + levelName + "\"");
      }
      writer.append("];\n</script>");
      writer.append("<script type=\"text/javascript\" src=\"" 
        + input.getResourcePath("old_grid/PartiturVisualizer.js") 
        + "\"></script>");
      writer.append("</body></html>");
      //END
    }
    catch(RemoteException e)
    {
      //ignore
    }
    catch(ParserConfigurationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(SAXException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  //An internal helper class
  private class PaulaInline2PartiturWriter
  {

    public class Token
    {

      private long id;
      private String text;

      public long getId()
      {
        return id;
      }

      public String getText()
      {
        return text;
      }
    }
    private Partitur partitur;
    private Set<String> namespaceSet;
    private Document document;

    public class SpanAnnotation extends HashMap<String, String>
    {

      private static final long serialVersionUID = 1L;
      private Long id;
      private String name;
      private long length;
      private long tokenId;
      private long offset;
      private Set<String> tokenIdSet = new HashSet<String>();

      public SpanAnnotation(long id, String name, long tokenId, long offset, long length)
      {
        this.id = id;
        this.name = name;
        this.tokenId = tokenId;
        this.offset = offset;
        this.length = length;

      }

      public Set<String> getTokenIdSet()
      {
        return this.tokenIdSet;
      }

      public long getTokenId()
      {
        return this.tokenId;
      }

      public long getOffset()
      {
        return this.offset;
      }

      public long getLength()
      {
        return this.length;
      }

      public void setLength(long length)
      {
        this.length = length;
      }

      public String getName()
      {
        return this.name;
      }

      public void setName(String name)
      {
        this.name = name;
      }

      public Long getId()
      {
        return this.id;
      }

      public void setId(Long id)
      {
        this.id = id;
      }

      public boolean overlap(SpanAnnotation span)
      {
        return (span != null &&
          (this.offset >= span.offset && span.offset + span.length > this.offset ||
          span.offset >= this.offset && this.offset + this.length > span.offset));
      }

      @Override
      public boolean equals(Object o)
      {
        if(o == null)
        {
          return false;
        }
        try
        {
          SpanAnnotation span = (SpanAnnotation) o;
          if(this.id == null && span.id != null)
          {
            return false;
          }
          else
          {
            return (this.id.equals(span.id));
          }
        }
        catch(ClassCastException e)
        {
          return false;
        }
      }
//			@Override 
//			public String toString() {
//				String str = this.id + ": " + this.name + ", " + this.length + " =>";
//				for(Entry<String, String> e : this.entrySet()) {
//					str += " " + e.getKey() + " = " + e.getValue() + ", ";
//				}
//				return str;
//			}
    }

    public class Partitur extends HashMap<Long, List<SpanAnnotation>>
    {

      private static final long serialVersionUID = 15981529469761587L;
      private List<Token> tokenList = new ArrayList<Token>();

      public SpanAnnotation getAnnotation(long id)
      {
        for(Collection<SpanAnnotation> aList : this.values())
        {
          for(SpanAnnotation a : aList)
          {
            if(a.id == id)
            {
              return a;
            }
          }
        }
        return null;
      }

      public void put(Long id, SpanAnnotation spanAnnotation)
      {
        try
        {
          if(this.get(id).contains(spanAnnotation))
          {
            return;
          }
          this.get(id).add(spanAnnotation);
        }
        catch(NullPointerException e)
        {
          List<SpanAnnotation> list = new ArrayList<SpanAnnotation>();
          list.add(spanAnnotation);
          this.put(id, list);
        }
      }

      @Override
      public List<SpanAnnotation> get(Object arg0)
      {
        List<SpanAnnotation> tmp = super.get(arg0);
        try
        {
          java.util.Collections.sort(tmp, new Comparator<SpanAnnotation>()
          {

            public int compare(SpanAnnotation o1, SpanAnnotation o2)
            {
              if(o1.offset == o2.offset)
              {
                return (o1.length > o2.length) ? -1 : 1;
              }
              return (o1.offset < o2.offset) ? -1 : 1;
            }
          });
        }
        catch(NullPointerException e)
        {
          //leave this unsorted
        }
        return tmp;
      }

      public List<Token> getTokenList()
      {
        return this.tokenList;
      }

      public long getTokenOffset(long tokenId)
      {
        for(int i = 0; i < tokenList.size(); i++)
        {
          if(tokenList.get(i).id == tokenId)
          {
            return i;
          }
        }
        return -1;
      }
    }

    public PaulaInline2PartiturWriter(Document document)
    {
      this.document = document;
    }

    private long getId(Node node) throws NullPointerException
    {
      return Long.parseLong(node.getAttributes().getNamedItem("_id").getNodeValue().replaceFirst("^[^0-9]+_", "").replaceFirst("_[0-9]+$", ""));
    }

    public Partitur getPartitur() throws IOException
    {
      this.partitur = new Partitur();

      //At first we fetch information about the token Nodes
      NodeList tokenNodeList = document.getElementsByTagName("tok");
      for(int j = 0; j < tokenNodeList.getLength(); j++)
      {
        Node tokenNode = tokenNodeList.item(j);
        Token token = new Token();
        token.id = getId(tokenNode);
        token.text = tokenNode.getTextContent();
        this.partitur.tokenList.add(token);
      }

      //Lets process all annotation relevant nodes
      for(String namespace : this.namespaceSet)
      {

        NodeList nodeList = this.document.getElementsByTagName("*");

        for(int i = 0; i < nodeList.getLength(); i++)
        {
          Node node = nodeList.item(i);
          if(!(node.getNodeName().matches("^" + namespace + "[.:].+") || namespace.equals(node.getNodeName())))
          {
            continue;
          }
          try
          {
            tokenNodeList = XPathAPI.selectNodeList(node, ".//tok");
            long id = getId(node);
            SpanAnnotation tmpSpan = partitur.getAnnotation(id);

            if(tmpSpan != null)
            {
              //this is an addition to an already known annotation
              tmpSpan.length += tokenNodeList.getLength();
              //TODO handle discont annotations!!!
            }
            else
            {
              //this is a new annotation
              long tokenId = getId(tokenNodeList.item(0));
              SpanAnnotation span = new SpanAnnotation(id, node.getNodeName(), tokenId, partitur.getTokenOffset(tokenId), tokenNodeList.getLength());
              NamedNodeMap attributes = node.getAttributes();
              for(int j = 0; j < attributes.getLength(); j++)
              {
                Node attribute = attributes.item(j);
                if(!attribute.getNodeName().startsWith("_") && attribute.getNodeValue() != null)
                {
                  span.put(attribute.getNodeName(), attribute.getNodeValue());
                }
              }
              partitur.put(span.tokenId, span);
              tmpSpan = span;
            }
            //adding all token ids to span
            for(int k = 0; k < tokenNodeList.getLength(); k++)
            {
              tmpSpan.tokenIdSet.add(tokenNodeList.item(k).getAttributes().getNamedItem("_id").getNodeValue());
            }
          }
          catch(TransformerException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }
      }



//			this.offset = 0;
//			
//			buildDefinition(rootNode, null);
      return this.partitur;
    }

    public void setNamespaceSet(Set<String> namespaceSet)
    {
      this.namespaceSet = namespaceSet;
    }
  }
}
