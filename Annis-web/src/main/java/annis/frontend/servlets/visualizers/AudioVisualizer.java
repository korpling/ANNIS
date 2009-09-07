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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thomas
 */
public class AudioVisualizer extends WriterVisualizer
{

  private long audioID = 0;
  
  @Override
  public void writeOutput(Writer writer)
  {
    audioID = -1;
    
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser;

    DefaultHandler handler = new DefaultHandler()
    {

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
      {
        if(audioID < 0)
        {
          if(qName.equalsIgnoreCase("audio:audioFileSeg"))
          {
            String s = attributes.getValue("exmaralda:AUDIO");
            try
            {
              audioID = Long.parseLong(s);
            }
            catch(NumberFormatException ex)
            {

            }
          }
          else if(attributes.getValue("audio:audioFile") != null)
          {
            try
            {
              audioID = Long.parseLong(attributes.getValue("audio:audioFile"));
            }
            catch(NumberFormatException ex)
            {
              
            }
          }
        }
      }
      
    };
    try
    {
      saxParser = factory.newSAXParser();
      saxParser.parse(new ByteArrayInputStream(getPaula().getBytes()), handler);

    }
    catch(Exception ex)
    {
      Logger.getLogger(AudioVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    try
    {

      writer.append(
        "<embed src=\"" + getContextPath() + "/mediaplayer/mediaplayer.swf\" " +
        "width=\"400\" height=\"300\" allowscriptaccess=\"always\" allowfullscreen=\"false\" " +
        "flashvars=\"width=400&height=300&file=" + getContextPath() + "/secure/Binary/" + audioID + ".mp3\"	/>");

    }
    catch(IOException ex)
    {
      Logger.getLogger(AudioVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
