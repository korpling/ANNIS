/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.controlpanel;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.jsclipboard.JSClipboard;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.ListSelect;
import java.util.List;
import org.corpus_tools.annis.api.model.BadRequestError;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.ExportPanel;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.HistoryPanel;
import org.corpus_tools.annis.gui.IDGenerator;
import org.corpus_tools.annis.gui.components.VirtualKeyboardCodeEditor;
import org.corpus_tools.annis.gui.components.codemirror.AqlCodeEditor;
import org.corpus_tools.annis.gui.frequency.FrequencyQueryPanel;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.corpus_tools.annis.gui.querybuilder.QueryBuilderChooser;
import org.vaadin.hene.popupbutton.PopupButton;

/**
 *
 * @author thomas
 */
public class QueryPanel extends GridLayout implements ValueChangeListener {

    private class ShowExportClickListener implements ClickListener {
        /**
         * 
         */
        private static final long serialVersionUID = -6210045386073348322L;
        private AnnisUI ui;
        private ExportPanel panel;

        public ShowExportClickListener(AnnisUI ui) {
            this.ui = ui;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            if (panel == null) {
              panel = new ExportPanel(ui);
            }

            final TabSheet tabSheet = ui.getSearchView().getMainTab();
            Tab tab = tabSheet.getTab(panel);

            if (tab == null) {
                tab = tabSheet.addTab(panel, "Export");
                tab.setIcon(FontAwesome.DOWNLOAD);
            }

            tab.setClosable(true);
            tabSheet.setSelectedTab(panel);

            btMoreActions.setPopupVisible(false);
        }

    }

    private class ShowFrequencyClickListener implements ClickListener {
        /**
         * 
         */
        private static final long serialVersionUID = 254430075328361254L;
        private AnnisUI ui;

        public ShowFrequencyClickListener(AnnisUI ui) {
            this.ui = ui;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            if (frequencyPanel == null) {
                frequencyPanel = new FrequencyQueryPanel(ui.getQueryController(), state);
                txtQuery.addTextChangeListener(frequencyPanel);
            }

            final TabSheet tabSheet = ui.getSearchView().getMainTab();
            Tab tab = tabSheet.getTab(frequencyPanel);

            if (tab == null) {
                tab = tabSheet.addTab(frequencyPanel, "Frequency Analysis");
                tab.setIcon(FontAwesome.BAR_CHART_O);
            }

            tab.setClosable(true);
            tabSheet.setSelectedTab(frequencyPanel);

            btMoreActions.setPopupVisible(false);
        }

    }

    private static class ShowKeyboardClickListener implements ClickListener {

        /**
         * 
         */
        private static final long serialVersionUID = 4930785605811315596L;
        private final VirtualKeyboardCodeEditor virtualKeyboard;

        public ShowKeyboardClickListener(VirtualKeyboardCodeEditor virtualKeyboard) {
            this.virtualKeyboard = virtualKeyboard;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            virtualKeyboard.show();
        }
    }

    private static class ShowQueryBuilderClickListener implements ClickListener {

        /**
         * 
         */
        private static final long serialVersionUID = -9049128218509047338L;
        private QueryBuilderChooser queryBuilder;
        private AnnisUI ui;

        public ShowQueryBuilderClickListener(AnnisUI ui) {
            this.ui = ui;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            if (queryBuilder == null) {
              queryBuilder = new QueryBuilderChooser(ui);
            }
            final TabSheet tabSheet = ui.getSearchView().getMainTab();
            Tab tab = tabSheet.getTab(queryBuilder);

            if (tab == null) {
                tab = tabSheet.addTab(queryBuilder, "Query Builder",
                        new ThemeResource("images/tango-icons/16x16/document-properties.png"));

                ui.addAction(new ShortcutListener("^Query builder") {
                    @Override
                    public void handleAction(Object sender, Object target) {
                        if (queryBuilder != null && tabSheet.getTab(queryBuilder) != null) {
                            tabSheet.setSelectedTab(queryBuilder);
                        }
                    }
                });
            }

            tab.setClosable(true);
            tabSheet.setSelectedTab(queryBuilder);
        }

    }

    public class ShowResultClickListener implements Button.ClickListener {

        /**
         * 
         */
        private static final long serialVersionUID = -531082340927506846L;

        @Override
        public void buttonClick(ClickEvent event) {
            if (ui != null && ui.getQueryController() != null) {
                ui.getQueryController().executeSearch(true, true);
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8843266533468321213L;
    public static final int MAX_HISTORY_MENU_ITEMS = 5;
    private AqlCodeEditor txtQuery;
    private TextArea txtStatus;
    private Button btShowResult;
    // private Button btShowResultNewTab;
    private PopupButton btHistory;
    private final ListSelect lstHistory;
    private final QueryUIState state;

    private ProgressBar piCount;

    private String lastPublicStatus;;

    private Window historyWindow;

    private PopupButton btMoreActions;

    private FrequencyQueryPanel frequencyPanel;

    private final AnnisUI ui;

    private final BeanItemContainer<Query> historyContainer = new BeanItemContainer<>(Query.class);
    private Button btShowQueryBuilder = new Button("Query<br />Builder");

    public QueryPanel(final AnnisUI ui) {
        super(4, 5);
        this.ui = ui;

        this.lastPublicStatus = "Welcome to ANNIS! " + "A tutorial is available on the right side.";

        this.state = ui.getQueryState();

        setSpacing(true);
        setMargin(false);

        setRowExpandRatio(0, 1.0f);
        setColumnExpandRatio(0, 0.0f);
        setColumnExpandRatio(1, 0.1f);
        setColumnExpandRatio(2, 0.0f);
        setColumnExpandRatio(3, 0.0f);

        txtQuery = new AqlCodeEditor();
        txtQuery.setPropertyDataSource(state.getAql());
        txtQuery.setInputPrompt("Please enter AQL query");
        txtQuery.addStyleName("query");
        if (ui.getInstanceFont() == null) {
            txtQuery.addStyleName("default-query-font");
            txtQuery.setTextareaStyle("default-query-font");
        } else {
            txtQuery.addStyleName(Helper.CORPUS_FONT);
            txtQuery.setTextareaStyle(Helper.CORPUS_FONT);
        }

        txtQuery.addStyleName("keyboardInput");
        txtQuery.setWidth("100%");
        txtQuery.setHeight(15f, Unit.EM);
        txtQuery.setTextChangeTimeout(500);

        final VirtualKeyboardCodeEditor virtualKeyboard;
        if (ui.getInstanceConfig().getKeyboardLayout() == null) {
            virtualKeyboard = null;
        } else {
            virtualKeyboard = new VirtualKeyboardCodeEditor();
            virtualKeyboard.setKeyboardLayout(ui.getInstanceConfig().getKeyboardLayout());
            virtualKeyboard.extend(txtQuery);
        }

        txtStatus = new TextArea();
        txtStatus.setValue(this.lastPublicStatus);
        txtStatus.setWidth("100%");
        txtStatus.setHeight(4.0f, Unit.EM);
        txtStatus.addStyleName("border-layout");
        txtStatus.setReadOnly(true);

        piCount = new ProgressBar();
        piCount.setIndeterminate(true);
        piCount.setEnabled(false);
        piCount.setVisible(false);

        btShowResult = new Button("Search");
        btShowResult.setIcon(FontAwesome.SEARCH);
        btShowResult.setWidth("100%");
        btShowResult.addClickListener(new ShowResultClickListener());
        btShowResult.setDescription("<strong>Show Result</strong><br />Ctrl + Enter", ContentMode.HTML);
        btShowResult.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);
        btShowResult.setDisableOnClick(true);

        VerticalLayout historyListLayout = new VerticalLayout();
        historyListLayout.setSizeUndefined();

        lstHistory = new ListSelect();
        lstHistory.setWidth("200px");
        lstHistory.setNullSelectionAllowed(false);
        lstHistory.setValue(null);
        lstHistory.addValueChangeListener(this);
        lstHistory.setImmediate(true);
        lstHistory.setContainerDataSource(historyContainer);
        lstHistory.setItemCaptionPropertyId("query");
        lstHistory.addStyleName(Helper.CORPUS_FONT);

        Button btShowMoreHistory = new Button("Show more details", (ClickListener) event -> {
            if (historyWindow == null) {
                historyWindow = new Window("History");
                historyWindow.setModal(false);
                historyWindow.setWidth("400px");
                historyWindow.setHeight("250px");
            }
            historyWindow.setContent(new HistoryPanel(state.getHistory(), ui.getQueryController()));

            if (UI.getCurrent().getWindows().contains(historyWindow)) {
                historyWindow.bringToFront();
            } else {
                UI.getCurrent().addWindow(historyWindow);
            }
        });
        btShowMoreHistory.setWidth("100%");

        historyListLayout.addComponent(lstHistory);
        historyListLayout.addComponent(btShowMoreHistory);

        historyListLayout.setExpandRatio(lstHistory, 1.0f);
        historyListLayout.setExpandRatio(btShowMoreHistory, 0.0f);

        btHistory = new PopupButton("History");
        btHistory.setContent(historyListLayout);
        btHistory.setDescription("<strong>Show History</strong><br />"
                + "Either use the short overview (arrow down) or click on the button " + "for the extended view.",
                ContentMode.HTML);

        Button btShowKeyboard = null;
        if (virtualKeyboard != null) {
            btShowKeyboard = new Button();
            btShowKeyboard.setWidth("100%");
            btShowKeyboard.setDescription("Click to show a virtual keyboard");
            btShowKeyboard.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btShowKeyboard.addStyleName(ValoTheme.BUTTON_SMALL);
            btShowKeyboard.setIcon(new ClassResource(VirtualKeyboardCodeEditor.class, "keyboard.png"));
            btShowKeyboard.addClickListener(new ShowKeyboardClickListener(virtualKeyboard));
        }
        final JSClipboard clipboard = new JSClipboard();
        Button btCopy = new Button("");
        btCopy.setWidth("100%");
        btCopy.setDescription("Copy query to clipboard");
        btCopy.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btCopy.addStyleName(ValoTheme.BUTTON_SMALL);
        btCopy.setIcon(FontAwesome.COPY);

        clipboard.apply(btCopy, txtQuery);
        clipboard.setText(state.getAql().getValue());
        state.getAql().addValueChangeListener(event -> clipboard.setText(event.getProperty().getValue().toString()));

        clipboard.addSuccessListener(() -> Notification.show("Copied AQL to clipboard"));

        btShowQueryBuilder.setHtmlContentAllowed(true);
        btShowQueryBuilder.addStyleName(ValoTheme.BUTTON_SMALL);
        btShowQueryBuilder.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        btShowQueryBuilder.setIcon(new ThemeResource("images/tango-icons/32x32/document-properties.png"));
        btShowQueryBuilder.addClickListener(new ShowQueryBuilderClickListener(ui));

        VerticalLayout moreActionsLayout = new VerticalLayout();
        moreActionsLayout.setWidth("250px");
        btMoreActions = new PopupButton("More");
        btMoreActions.setContent(moreActionsLayout);

        // btShowResultNewTab = new Button("Search (open in new tab)");
        // btShowResultNewTab.setWidth("100%");
        // btShowResultNewTab.addClickListener(new ShowResultInNewTabClickListener());
        // btShowResultNewTab.setDescription("<strong>Show Result and open result in new
        // tab</strong><br />Ctrl + Shift + Enter");
        // btShowResultNewTab.setDisableOnClick(true);
        // btShowResultNewTab.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL,
        // ModifierKey.SHIFT);
        // moreActionsLayout.addComponent(btShowResultNewTab);

        Button btShowExport = new Button("Export", new ShowExportClickListener(ui));
        btShowExport.setIcon(FontAwesome.DOWNLOAD);
        btShowExport.setWidth("100%");
        moreActionsLayout.addComponent(btShowExport);

        Button btShowFrequency = new Button("Frequency Analysis", new ShowFrequencyClickListener(ui));
        btShowFrequency.setIcon(FontAwesome.BAR_CHART_O);
        btShowFrequency.setWidth("100%");
        moreActionsLayout.addComponent(btShowFrequency);

        /*
         * We use the grid layout for a better rendering efficiency, but this comes with
         * the cost of some complexity when defining the positions of the elements in
         * the layout.
         * 
         * This grid hopefully helps a little bit in understanding the "magic" numbers
         * better.
         * 
         * Q: Query text field QB: Button to toggle query builder // TODO KEY: Button to
         * show virtual keyboard COPY: Button for copying the text SEA: "Search" button
         * MOR: "More actions" button HIST: "History" button STAT: Text field with the
         * real status PROG: indefinite progress bar (spinning circle)
         * 
         * \ 0 | 1 | 2 | 3 --+-----+---+---+---+----- 0 | Q | Q | Q | QB
         * --+-----+-----+-----+----- 1 | Q | Q | Q | KEY --+-----+-----+-----+----- 2 |
         * Q | Q | Q | COPY --+-----+-----+-----+----- 3 | SEA | MOR | HIST|
         * --+-----+-----+-----+----- 4 | STAT| STAT| STAT| PROG
         */
        addComponent(txtQuery, 0, 0, 2, 2);
        addComponent(txtStatus, 0, 4, 2, 4);
        addComponent(btShowResult, 0, 3);
        addComponent(btMoreActions, 1, 3);
        addComponent(btHistory, 2, 3);
        addComponent(piCount, 3, 4);
        addComponent(btShowQueryBuilder, 3, 0);
        if (btShowKeyboard != null) {
            addComponent(btShowKeyboard, 3, 1);
            addComponent(btCopy, 3, 2);
        } else {
            addComponent(btCopy, 3, 1);
        }

        // alignment
        setRowExpandRatio(0, 0.0f);
        setRowExpandRatio(1, 0.0f);
        setRowExpandRatio(2, 1.0f);
        setColumnExpandRatio(0, 1.0f);
        setColumnExpandRatio(1, 0.0f);
        setColumnExpandRatio(2, 0.0f);
        setColumnExpandRatio(3, 0.0f);

        // setComponentAlignment(btShowQueryBuilder, Alignment.BOTTOM_CENTER);
    }

    @Override
    public void attach() {
        super.attach();
        IDGenerator.assignIDForFields(QueryPanel.this, btShowResult, btMoreActions,
                btShowQueryBuilder);

    }

    public String getLastPublicStatus() {
        return lastPublicStatus;
    }

    public ProgressBar getPiCount() {
        return piCount;
    }

    public String getQuery() {
        if (txtQuery != null) {
            return txtQuery.getValue();
        }
        return "";
    }

    // public class ShowResultInNewTabClickListener implements Button.ClickListener
    // {
    //
    // @Override
    // public void buttonClick(ClickEvent event)
    // {
    // if(controller != null)
    // {
    // controller.setQuery((txtQuery.getValue()));
    // controller.executeQuery(false);
    // }
    // }
    // }

    public void notifyFrequencyTabClose() {
        txtQuery.removeTextChangeListener(frequencyPanel);
        frequencyPanel = null;
    }

    public void setCountIndicatorEnabled(boolean enabled) {
        if (piCount != null && btShowResult != null && txtStatus != null) {
            if (enabled) {
                if (!piCount.isVisible()) {
                    piCount.setVisible(true);
                    piCount.setEnabled(true);
                }
            }

            btShowResult.setEnabled(!enabled);
            // btShowResultNewTab.setEnabled(!enabled);
        }
    }

    public void setError(BadRequestError error) {
        txtQuery.setError(error);
    }

    public void setNodes(List<QueryAttributeDescription> nodes) {
        txtQuery.setNodes(nodes);
    }

    public void setQuery(String query) {
        if (txtQuery != null) {
            txtQuery.setValue(query);
        }
    }

    public void setStatus(String status) {
        if (txtStatus != null) {
            txtStatus.setReadOnly(false);
            txtStatus.setValue(status);
            lastPublicStatus = status;
            txtStatus.setReadOnly(true);
        }
    }

    public void setStatus(String status, String resultStatus) {
        if (txtStatus != null) {
            txtStatus.setReadOnly(false);
            txtStatus.setValue(status + resultStatus);
            lastPublicStatus = status;
            txtStatus.setReadOnly(true);
        }
    }

    public void updateShortHistory() {
        historyContainer.removeAllItems();

        int counter = 0;

        for (Query q : state.getHistory().getItemIds()) {
            if (counter >= MAX_HISTORY_MENU_ITEMS) {
                break;
            } else {
                historyContainer.addBean(q);
            }
            counter++;
        }
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        btHistory.setPopupVisible(false);
        Object q = event.getProperty().getValue();
        if (ui != null && ui.getQueryController() != null && q instanceof Query) {
            ui.getQueryController().setQuery((Query) q);
        }
    }

}
