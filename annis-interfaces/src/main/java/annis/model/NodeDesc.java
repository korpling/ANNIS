package annis.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeDesc {

  private long componentNr;
  private String aqlFragment;
  private String variable;
  private String annoName;

  public String getAnnoName() {
    return annoName;
  }

  public String getAqlFragment() {
    return aqlFragment;
  }

  public long getComponentNr() {
    return componentNr;
  }

  public String getVariable() {
    return variable;
  }

  public void setAnnoName(String annoName) {
    this.annoName = annoName;
  }

  public void setAqlFragment(String aqlFragment) {
    this.aqlFragment = aqlFragment;
  }

  public void setComponentNr(long componentNr) {
    this.componentNr = componentNr;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }


}
