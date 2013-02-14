package annis.gui.widgets;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.net.URI;
import java.util.Map;

/**
 * Server side component for the VAutoHeightIFrame widget.
 */
public class AutoHeightIFrame extends AbstractComponent implements LegacyComponent
{

  private URI uri;
  private boolean heightWasSet = false;
  public static final int ADDITIONAL_HEIGHT = 25;
  
  public final static String RES_KEY = "iframe-vis-res";

  public AutoHeightIFrame(URI uri)
  {
     this.uri = uri;  
     setWidth("100%");
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    target.addAttribute("url", uri.toASCIIString());
    target.addAttribute("additional_height", ADDITIONAL_HEIGHT);
  }  

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {
    if (!heightWasSet && variables.containsKey("height"))
    {
      int height = (Integer) variables.get("height");
//      getWindow().showNotification("new height: " + height, Window.Notification.TYPE_TRAY_NOTIFICATION);
      this.setHeight((float) height, Sizeable.UNITS_PIXELS);
      heightWasSet = true;
    }   
  }
}
