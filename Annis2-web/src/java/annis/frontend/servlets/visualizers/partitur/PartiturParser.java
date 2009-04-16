
package annis.frontend.servlets.visualizers.partitur;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

/**
 *
 * @author thomas
 */
public class PartiturParser implements Serializable
{

  private List<Token> token;
  private Set<String> knownTiers;
  
  private HashMap<String, String> tier2ns;
  
  public PartiturParser(Document paula, String namespace)
    throws JDOMException
  {
    token = new LinkedList<Token>();
    knownTiers = new HashSet<String>();
    
    tier2ns = new HashMap<String, String>();
    
    Iterator<Element> itDesc = paula.getDescendants(new ElementFilter());
    
    
    while(itDesc.hasNext())
    {
      Element e = itDesc.next();
      
      if("".equals(e.getNamespacePrefix()) && "tok".equals(e.getName()))
      {
        long tokenID = e.getAttribute("_id").getLongValue();
        
        Token currentToken = new Token(tokenID, new Hashtable<String, Event>(), e.getValue()); 
        
        token.add(currentToken);
        
        // get parent annotations matching namespace
        Element curAnno = e.getParentElement();
        while(curAnno != null)
        {
          if(namespace.equals(curAnno.getNamespacePrefix()))
          {
            List<Attribute> attributes = curAnno.getAttributes();
            for(Attribute a : attributes)
            {
              if(namespace.equals(a.getNamespacePrefix()))
              {
                // finally, put this annotation in the list
                Event newEvent = new Event(curAnno.getAttribute("_id").getLongValue(), 
                  a.getValue());
                currentToken.getTier2Event().put(a.getName(), newEvent);
                // update our set of tiers
                knownTiers.add(a.getName());
                tier2ns.put(a.getName(), a.getNamespacePrefix());
              }
            }
          }
          curAnno = curAnno.getParentElement();
        }
        
      }
    }
    
    // now connect the token to make it easier later to find the neighbors
    Iterator<Token> it = token.iterator();
    
    Token current = it.hasNext() ? it.next() : null;
    Token next = it.hasNext() ? it.next() : null;
    Token last = null;
    
    while(current != null)
    {
      current.setBefore(last);
      current.setAfter(next);
      
      last = current;
      current = next;
      next = it.hasNext() ? it.next() : null;
    }
    
  }

  public Set<String> getKnownTiers()
  {
    return knownTiers;
  }

  public String namespaceForTier(String tier)
  {
    return tier2ns.get(tier);
  }
  
  
  
  public List<Token> getToken()
  {
    return token;
  }
  
  public class Token implements Serializable
  {
    private Map<String,Event> tier2Event;
    private long id;
    private String value;
    
    private Token before;
    private Token after;
    
    public Token(long id, Map<String, Event> tier2Event, String value)
    {
      this.tier2Event = tier2Event;
      this.id = id;
      this.value = value;
      before = null;
      after = null;
    }

    public Map<String, Event> getTier2Event()
    {
      return tier2Event;
    }

    public long getId()
    {
      return id;
    }

    public Token getAfter()
    {
      return after;
    }

    public void setAfter(Token after)
    {
      this.after = after;
    }

    public Token getBefore()
    {
      return before;
    }

    public void setBefore(Token before)
    {
      this.before = before;
    }

    public String getValue()
    {
      return value;
    }
    
    
    
  }
  
  public class Event implements Serializable
  {
    private long id;
    private String value;
    
    public Event(long id, String value)
    {
      this.id = id;
      this.value = value;
    }

    public long getId()
    {
      return id;
    }

    public String getValue()
    {
      return value;
    }

  }
}
