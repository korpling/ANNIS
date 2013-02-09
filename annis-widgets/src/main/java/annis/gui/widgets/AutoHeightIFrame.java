package annis.gui.widgets;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.Map;

/**
 * Server side component for the VAutoHeightIFrame widget.
 */
public class AutoHeightIFrame extends AbstractComponent implements LegacyComponent
{

  private ResourceReference resRef;
  private boolean urlUpdated = false;
  public static final int ADDITIONAL_HEIGHT = 25;
  
  public final static String RES_KEY = "iframe-vis-res";

  public AutoHeightIFrame(Resource resource)
  {
    this.resRef = ResourceReference.create(
                resource, (ClientConnector) this, RES_KEY);
    
    if (this.resRef == null)
    {
      getState().resources.remove(RES_KEY);
    }
    else
    {
      getState().resources.put(RES_KEY, this.resRef);
    }
    
    urlUpdated = false;
    setWidth("100%");
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {

    if (!urlUpdated)
    {
      target.addAttribute("url", resRef.getURL());
      target.addAttribute("additional_height", ADDITIONAL_HEIGHT);
      urlUpdated = true;
    }

  }

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {
    if (variables.containsKey("height"))
    {
      int height = (Integer) variables.get("height");
//      getWindow().showNotification("new height: " + height, Window.Notification.TYPE_TRAY_NOTIFICATION);
      this.setHeight((float) height, Sizeable.UNITS_PIXELS);
    }   
  }
}
