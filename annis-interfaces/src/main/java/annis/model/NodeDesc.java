package annis.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeDesc {

    private long componentNr;
    private String aqlFragment;
    private String variable;
    private String annoName;
    
    public long getComponentNr() {
        return componentNr;
    }
    public void setComponentNr(long componentNr) {
        this.componentNr = componentNr;
    }
    public String getAqlFragment() {
        return aqlFragment;
    }
    public void setAqlFragment(String aqlFragment) {
        this.aqlFragment = aqlFragment;
    }
    public String getVariable() {
        return variable;
    }
    public void setVariable(String variable) {
        this.variable = variable;
    }
    public String getAnnoName() {
        return annoName;
    }
    public void setAnnoName(String annoName) {
        this.annoName = annoName;
    }
    
    
}
