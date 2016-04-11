package annis.gui.widgets;

import annis.gui.widgets.gwt.client.ui.autoheightiframe.AutoHeightIFrameServerRpc;
import annis.gui.widgets.gwt.client.ui.autoheightiframe.AutoHeightIFrameState;
import com.vaadin.ui.AbstractComponent;
import java.net.URI;

/**
 * Server side component for the VAutoHeightIFrame widget.
 */
public class AutoHeightIFrame extends AbstractComponent
{

  private boolean heightWasSet = false;

  public static final int ADDITIONAL_HEIGHT = -1;

  public final static String RES_KEY = "iframe-vis-res";

  public AutoHeightIFrame(URI uri)
  {
    setWidth("100%");
    registerRpc(rpc);
    
    getState().setUrl(uri.toASCIIString());
    getState().setAdditionalHeight(ADDITIONAL_HEIGHT);
  }
  
  private AutoHeightIFrameServerRpc rpc = new AutoHeightIFrameServerRpc()
  {
    @Override
    public void newHeight(float height)
    {
      if (!heightWasSet)
      {
        setHeight(height, Unit.PIXELS);
        heightWasSet = true;
      }
    }
  };

  @Override
  protected AutoHeightIFrameState getState()
  {
    return (AutoHeightIFrameState) super.getState();
  }
  
}
