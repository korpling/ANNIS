package annis.gui;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.declarative.Design;

import annis.gui.beans.CorpusBrowserEntry;
import annis.libgui.Helper;

@DesignRoot
public class ExampleTable extends Table
{
  

  public ExampleTable()
  {
    Design.read("ExampleTable.html", this);
  }
  
  public void setCitationLinkGenerator(CitationLinkGenerator citationGenerator)
  {
    addGeneratedColumn("genlink", citationGenerator);
  }
  
  @Override
  public void attach()
  {
    super.attach();
    
    addGeneratedColumn("example", new ColumnGenerator()
    {
      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        CorpusBrowserEntry corpusBrowserEntry = (CorpusBrowserEntry) itemId;
        Label l = new Label(corpusBrowserEntry.getExample());
        l.setContentMode(ContentMode.TEXT);
        l.addStyleName(Helper.CORPUS_FONT_FORCE);
        return l;
      }
    });

    setVisibleColumns("name", "example", "genlink");
    setColumnHeaders("Name", "Example (click to use query)", "URL");
    setColumnExpandRatio("name", 0.3f);
    setColumnExpandRatio("example", 0.7f);
    setImmediate(true);
  }
}