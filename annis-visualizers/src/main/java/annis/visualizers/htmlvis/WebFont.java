package annis.visualizers.htmlvis;


import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
/**
 * A simple description of a web-font reference.
 * TODO: use enums for style
 * @author thomas
 *
 */
public class WebFont
{
  /** a map of source URLs to a format name. */
  private Map<String, String> sources = new LinkedHashMap<>();
  private String name;
  private String weight = "400";
  private String style = "normal";
  
  public Map<String, String> getSources()
  {
    return sources;
  }
  public void setSources(Map<String, String> source)
  {
    this.sources = source;
  }
  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name = name;
  }
  public String getWeight()
  {
    return weight;
  }
  public void setWeight(String weight)
  {
    this.weight = weight;
  }
  public String getStyle()
  {
    return style;
  }
  public void setStyle(String style)
  {
    this.style = style;
  }
  
  
  
}
