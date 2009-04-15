/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class AudioVisualizer extends Visualizer
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
        if(qName.equalsIgnoreCase("audio:audioFileSeg"))
        {
          String s = attributes.getValue("exmaralda:AUDIO");
          audioID = Long.parseLong(s);
        }
      }
      
    };
    try
    {
      saxParser = factory.newSAXParser();
      saxParser.parse(new ByteArrayInputStream(super.paula.getBytes()), handler);

    }
    catch(Exception ex)
    {
      Logger.getLogger(AudioVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    try
    {

      writer.append(
        "<embed src=\"" + contextPath + "/mediaplayer/mediaplayer.swf\" " +
        "width=\"400\" height=\"300\" allowscriptaccess=\"always\" allowfullscreen=\"false\" " +
        "flashvars=\"width=400&height=300&file=" + contextPath + "/secure/Binary/" + audioID + ".mp3\"	/>");

    }
    catch(IOException ex)
    {
      Logger.getLogger(AudioVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
