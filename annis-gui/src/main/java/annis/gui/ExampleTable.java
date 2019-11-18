package annis.gui;

import com.vaadin.data.provider.GridSortOrder;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

import annis.gui.beans.CorpusBrowserEntry;
import annis.libgui.Helper;

public class ExampleTable extends Grid<CorpusBrowserEntry> {


  public ExampleTable() {
  }

  @Override
  public void attach() {
    super.attach();


    Column<CorpusBrowserEntry, String> colName = addColumn(CorpusBrowserEntry::getName);
    colName.setCaption("Name");
    colName.setExpandRatio(1);

    Column<CorpusBrowserEntry, String> colExample = addColumn(CorpusBrowserEntry::getExample);
    colExample.setCaption("Example (click to use query)");
    colExample.setExpandRatio(3);
    colExample.setStyleGenerator(cbe -> Helper.CORPUS_FONT_FORCE);
    
    addComponentColumn(cbe -> {
      Button btLink = new Button();
      btLink.addStyleName(ValoTheme.BUTTON_BORDERLESS);
      btLink.setIcon(FontAwesome.SHARE_ALT);
      btLink.setDescription("Share query reference link");
      btLink.addClickListener(new LinkClickListener(cbe));
      return btLink;
    });

    setSortOrder(GridSortOrder.asc(colName).thenAsc(colExample));

    setSizeFull();

  }

}