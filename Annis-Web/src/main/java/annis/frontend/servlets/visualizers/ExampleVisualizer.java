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
package annis.frontend.servlets.visualizers;

import java.io.StringReader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class ExampleVisualizer extends WriterVisualizer
{

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {
      //Retrieve DOM-Document for PAULA
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(input.getPaula())));

      //Use XPath API to retrieve all token from document
      NodeList tokenNodeList = XPathAPI.selectNodeList(document, ".//tok");

      //Write HTML preamble
      writer.append("<html><head><title>Output from ExampleVisualizer</title></head><body>");

      //Write output
      for(int i = 0; i < tokenNodeList.getLength(); i++)
      {
        Node tokenNode = tokenNodeList.item(i);

        //Use markableMap to set the color for this token
        String tokenId = tokenNode.getAttributes().getNamedItem("_id").getNodeValue();
        String color = input.getMarkableMap().containsKey(tokenId) ? input.getMarkableMap().get(tokenId) : "";

        writer.append("<font color=\"" + color + "\">" + tokenNode.getTextContent() + "</font> ");
      }

      //Write HTML footer
      writer.append("</body></html>");
    }
    catch(Exception e)
    {
      //Ignore all exceptions in this example
    }
  }
}
