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
    getState().setAdditionalHeight(-1);
  }

  @Override
  public AutoHeightIFrameState getState() {
    return (AutoHeightIFrameState) super.getState();
  }

}
