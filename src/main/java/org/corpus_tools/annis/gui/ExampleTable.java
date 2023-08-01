package org.corpus_tools.annis.gui;

import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.corpus_tools.annis.gui.corpusbrowser.CorpusBrowserEntry;

public class ExampleTable extends Grid<CorpusBrowserEntry> {

    private static final long serialVersionUID = 4240653432503717501L;

    public ExampleTable() {
        super(CorpusBrowserEntry.class);
    }

    @Override
    public void attach() {
        super.attach();

        setSizeFull();

        getColumn("name").setCaption("Name");

        Column<?, ?> exampleColumn = addColumn(
                cbe -> ("<div class=\"" + Helper.CORPUS_FONT_FORCE + "\">" + cbe.getExample() + "</div>"),
                new HtmlRenderer());
        exampleColumn.setId("genexample");
        exampleColumn.setCaption("Example (click to use query)");
        exampleColumn.setExpandRatio(1);
        setColumns("name", "genexample");

    }
}