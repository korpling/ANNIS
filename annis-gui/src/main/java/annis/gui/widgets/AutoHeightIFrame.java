package annis.gui.widgets;

import annis.gui.resultview.VisualizerPanel;
import annis.gui.widgets.gwt.client.VAutoHeightIFrame;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import java.util.Map;

/**
 * Server side component for the VAutoHeightIFrame widget.
 */
@ClientWidget(VAutoHeightIFrame.class)
public class AutoHeightIFrame extends AbstractComponent
{

  private String url;
  private boolean urlUpdated = false;
  private VisualizerPanel visPanel;
  public static final int ADDITIONAL_HEIGHT = 25;

  public AutoHeightIFrame(String url, VisualizerPanel parent)
  {
    this.url = url;
    urlUpdated = false;
    this.visPanel = parent;
    setWidth("100%");
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);

    if (!urlUpdated)
    {
      target.addAttribute("url", url);
      target.addAttribute("additional_height", ADDITIONAL_HEIGHT);
      urlUpdated = true;
    }

  }

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {
    if (variables.containsKey("height"))
    {
      this.setHeight("" + variables.get("height"));
      visPanel.startMediaVisFromKWIC();    
    }   
  }
}
