package annis.gui.widgets;

import annis.gui.widgets.gwt.client.VMyComponent;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Server side component for the VMyComponent widget.
 */
@ClientWidget(VMyComponent.class)
public class MyComponent extends AbstractComponent {

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        // TODO Paint any component specific content by setting attributes
        // These attributes can be read in updateFromUIDL in the widget.
    }

}
