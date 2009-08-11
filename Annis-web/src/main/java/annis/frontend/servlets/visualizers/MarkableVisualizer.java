/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
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
package annis.frontend.servlets.visualizers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MarkableVisualizer extends WriterVisualizer
{

  private class MarkableHandler extends DefaultHandler
  {

    private StringBuffer sBuffer = new StringBuffer();
    private Writer writer;
    private Set<String> markerSet = new HashSet<String>();
    private boolean marked = false;
    private Set<String> alreadySeen = new HashSet<String>();

    public MarkableHandler(Writer writer, Set<String> markerSet)
    {
      this.writer = writer;
      this.markerSet = markerSet;
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
      String id = atts.getValue("_id");
      if((this.marked = (this.markerSet.contains(id) && !this.alreadySeen.contains(id))) == true)
      {
        this.alreadySeen.add(id);
        sBuffer.append("\t<node type=\"" + qName + "\"");
        for(int i = 0; i < atts.getLength(); i++)
        {
          sBuffer.append(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");
        }
      }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
    {
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
      if(this.marked)
      {
        StringBuffer value = new StringBuffer();
        for(int i = start; i < (start + length); i++)
        {
          value.append(ch[i]);
        }
        String v = value.toString();
        v = v.trim();
        if(v.length() > 0)
        {
          sBuffer.append(">" + v + "</node>\n");
        }
        else
        {
          sBuffer.append("/>\n");
        }

        try
        {
          writer.append(sBuffer.toString());
        }
        catch(IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        sBuffer = new StringBuffer();
      }
      this.marked = false;

    }
  }

  @Override
  public void writeOutput(Writer writer)
  {
    try
    {
      writer.append("<result id=\"" + super.id + "\">\n");
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();

      DefaultHandler handler = new MarkableHandler(writer, super.markableMap.keySet());
      saxParser.parse(new ByteArrayInputStream(super.paula.getBytes()), handler);
      writer.append("</result>\n");
      writer.flush();
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
    catch(ParserConfigurationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public String getContentType()
  {
    return "text/xml";
  }
}
