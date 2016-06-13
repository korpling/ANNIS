package annis.visualizers.htmlvis;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebFontList
{
  private List<WebFont> webFonts = new ArrayList<>();

  @XmlElement(name="web-fonts")
  public List<WebFont> getWebFonts()
  {
    return webFonts;
  }

  public void setWebFonts(List<WebFont> webFonts)
  {
    this.webFonts = webFonts;
  }
  
  
  
}
