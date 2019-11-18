package annis.gui;

import java.io.Serializable;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;

import annis.QueryGenerator;
import annis.gui.beans.CitationProvider;
import annis.libgui.Helper;
import annis.model.DisplayedResultQuery;

public class LinkClickListener implements Button.ClickListener, Serializable {

    private final CitationProvider citationProvider;
    private final DisplayedResultQuery query;

    public LinkClickListener(CitationProvider citationProvider) {
        this.citationProvider = citationProvider;
        this.query = null;
    }
    
    public LinkClickListener(DisplayedResultQuery query)
    {
      this.citationProvider = null;
      this.query = query;
    }

    @Override
    public void buttonClick(ClickEvent event) {

        if (query != null) {
            ShareQueryReferenceWindow c = new ShareQueryReferenceWindow(query,
                    !Helper.isKickstarter(VaadinSession.getCurrent()));
            UI.getCurrent().addWindow(c);
            c.center();
        } else if (citationProvider != null) {
            ShareQueryReferenceWindow c = new ShareQueryReferenceWindow(
                    QueryGenerator.displayed().query(citationProvider.getQuery()).corpora(citationProvider.getCorpora())
                            .left(citationProvider.getLeftContext()).right(citationProvider.getRightContext()).offset(0)
                            .limit(10).build(),
                    !Helper.isKickstarter(VaadinSession.getCurrent()));
            UI.getCurrent().addWindow(c);
            c.center();
        } else {
            Notification.show("Internal error", "No valid citation link was found", Notification.Type.WARNING_MESSAGE);
        }

    }
}