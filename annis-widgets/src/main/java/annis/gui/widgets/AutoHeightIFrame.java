package annis.gui.widgets;

import annis.gui.widgets.gwt.client.ui.autoheightiframe.AutoHeightIFrameServerRpc;
import annis.gui.widgets.gwt.client.ui.autoheightiframe.AutoHeightIFrameState;
import com.vaadin.ui.AbstractComponent;
import java.net.URI;

/**
 * Server side component for the VAutoHeightIFrame widget.
 */
public class AutoHeightIFrame extends AbstractComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -9009947408098698710L;

  public static final int ADDITIONAL_HEIGHT = -1;

  public final static String RES_KEY = "iframe-vis-res";

  private boolean heightWasSet = false;

  private AutoHeightIFrameServerRpc rpc = height -> {
    if (!heightWasSet) {
      setHeight(height, Unit.PIXELS);
      heightWasSet = true;
    }
  };

  public AutoHeightIFrame(URI uri) {
    setWidth("100%");
    registerRpc(rpc);

    getState().setUrl(uri.toASCIIString());
    getState().setAdditionalHeight(ADDITIONAL_HEIGHT);
  }

  @Override
  protected AutoHeightIFrameState getState() {
    return (AutoHeightIFrameState) super.getState();
  }

}
