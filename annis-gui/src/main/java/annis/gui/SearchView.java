/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import annis.CommonHelper;
import annis.QueryGenerator;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.docbrowser.DocBrowserController;
import annis.gui.frequency.FrequencyQueryPanel;
import annis.gui.media.MediaControllerImpl;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import annis.libgui.media.MediaController;
import annis.libgui.media.MimeTypeErrorListener;
import annis.libgui.media.PDFController;
import annis.libgui.media.PDFControllerImpl;
import annis.model.DisplayedResultQuery;
import annis.model.PagedResultQuery;
import annis.model.Query;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.QueryLanguage;
import com.google.common.base.Splitter;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.GenericType;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.slf4j.LoggerFactory;

/**
 * The view which shows the search interface.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class SearchView extends GridLayout
        implements View, MimeTypeErrorListener, Page.UriFragmentChangedListener,
        TabSheet.CloseHandler, LoginListener, Sidebar, TabSheet.SelectedTabChangeListener {

    private static class AnnisCorpusListType extends GenericType<List<AnnisCorpus>> {

        public AnnisCorpusListType() {}
    }

    private class CitationRequestHandler implements RequestHandler {

        /**
         * 
         */
        private static final long serialVersionUID = -5700172209282345278L;

        @Override
        public boolean handleRequest(VaadinSession session, VaadinRequest request,
                VaadinResponse response) throws IOException {
            checkCitation();
            return false;
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4726183716126582129L;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SearchView.class);

    public static final String NAME = "";

    private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

    public final static int CONTROL_PANEL_WIDTH = 400;

    // regular expression matching, CLEFT and CRIGHT are optional
    // indexes: AQL=1, CIDS=2, CLEFT=4, CRIGHT=6
    private final Pattern citationPattern = Pattern.compile(
            "AQL\\((.*)\\),CIDS\\(([^)]*)\\)(,CLEFT\\(([^)]*)\\),)?(CRIGHT\\(([^)]*)\\))?",
            Pattern.MULTILINE | Pattern.DOTALL);

    private ControlPanel controlPanel;

    private TabSheet mainTab;

    private String lastEvaluatedFragment;

    private DocBrowserController docBrowserController;

    private Set<Component> selectedTabHistory;

    private final AnnisUI ui;

    private MainToolbar toolbar;

    private HelpPanel helpPanel;

    public SearchView(AnnisUI ui) {
        super(2, 2);
        this.ui = ui;
        this.selectedTabHistory = new LinkedHashSet<>();

        // init a doc browser controller
        this.docBrowserController = new DocBrowserController(ui);

        setSizeFull();
        setMargin(false);
        setRowExpandRatio(1, 1.0f);
        setColumnExpandRatio(1, 1.0f);

        helpPanel = new HelpPanel(ui);

        mainTab = new TabSheet();
        mainTab.setSizeFull();
        mainTab.setCloseHandler(SearchView.this);
        mainTab.addStyleName(ValoTheme.TABSHEET_FRAMED);
        mainTab.addSelectedTabChangeListener(SearchView.this);

        TabSheet.Tab helpTab = mainTab.addTab(helpPanel, "Help/Examples");
        helpTab.setIcon(FontAwesome.QUESTION_CIRCLE);
        helpTab.setClosable(false);

        controlPanel = new ControlPanel(ui, helpPanel);

        controlPanel.setWidth(CONTROL_PANEL_WIDTH, Layout.Unit.PIXELS);
        controlPanel.setHeight(100f, Layout.Unit.PERCENTAGE);

        ui.addAction(new ShortcutListener("Tutor^eial") {
            /**
             * 
             */
            private static final long serialVersionUID = 4230090114473234656L;

            @Override
            public void handleAction(Object sender, Object target) {
            mainTab.setSelectedTab(helpPanel);
            }
        });

        addComponent(controlPanel, 0, 1);
        addComponent(mainTab, 1, 1);
    }

    public void checkCitation() {
        if (VaadinSession.getCurrent() == null || VaadinSession.getCurrent().getSession() == null) {
            return;
        }
        Object origURLRaw = VaadinSession.getCurrent().getSession().getAttribute("citation");
        if (origURLRaw == null || !(origURLRaw instanceof String)) {
            return;
        }
        String origURL = (String) origURLRaw;
        String parameters = origURL.replaceAll(".*?/Cite(/)?", "");
        if (!"".equals(parameters) && !origURL.equals(parameters)) {
            try {
                String decoded = URLDecoder.decode(parameters, "UTF-8");
                evaluateCitation(decoded);
            } catch (UnsupportedEncodingException ex) {
                log.error(null, ex);
            }
        }

    }

    public void closeTab(Component c) {
        selectedTabHistory.remove(c);
        mainTab.removeComponent(c);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        if (event.getOldView() == event.getNewView()) {
            return;
        }

        InstanceConfig config = ui.getInstanceConfig();

        Page.getCurrent().setTitle(config.getInstanceDisplayName() + " (ANNIS Corpus Search)");

        Page.getCurrent().addUriFragmentChangedListener(this);

        getSession().addRequestHandler(new CitationRequestHandler());

        getSession().setAttribute(MediaController.class, new MediaControllerImpl());

        getSession().setAttribute(PDFController.class, new PDFControllerImpl());

        checkCitation();
        lastEvaluatedFragment = "";
        evaluateFragment(Page.getCurrent().getUriFragment());

        if (config.isLoginOnStart() && toolbar != null && Helper.getUser(ui) == null) {
            toolbar.showLoginWindow(false);
        }

    }

    public void evaluateCitation(String relativeUri) {
        Matcher m = citationPattern.matcher(relativeUri);
        if (m.matches()) {
            // AQL
            String aql = "";
            if (m.group(1) != null) {
                aql = m.group(1);
            }

            // CIDS
            Set<String> selectedCorpora = new HashSet<>();
            if (m.group(2) != null) {
                String[] cids = m.group(2).split(",");
                selectedCorpora.addAll(Arrays.asList(cids));
            }

            // filter by actually avaible user corpora in order not to get any exception
            // later
            CorporaApi api = new CorporaApi(Helper.getClient(ui));
            try {
              List<String> userCorpora = api.listCorpora();
              selectedCorpora.retainAll(userCorpora);
            } catch (ApiException ex) {
              log.error("Could not get list of corpora", ex);
            }

            // CLEFT and CRIGHT
            if (m.group(4) != null && m.group(6) != null) {
                int cleft = 0;
                int cright = 0;
                try {
                    cleft = Integer.parseInt(m.group(4));
                    cright = Integer.parseInt(m.group(6));
                } catch (NumberFormatException ex) {
                    log.error("could not parse context value", ex);
                }
                ui.getQueryController().setQuery(
                        new PagedResultQuery(cleft, cright, 0, 10, null, aql, selectedCorpora));
            } else {
                ui.getQueryController()
                        .setQuery(new Query(aql, QueryLanguage.AQL_QUIRKS_V3, selectedCorpora));
            }

            // remove all currently openend sub-windows
            Set<Window> all = new HashSet<>(ui.getWindows());
            for (Window w : all) {
                ui.removeWindow(w);
            }
        } else {
            Notification.show("Invalid citation", Notification.Type.WARNING_MESSAGE);
        }

    }

    private void evaluateFragment(String fragment) {
        // do nothing if not changed
        if (fragment == null || fragment.isEmpty() || fragment.equals(lastEvaluatedFragment)) {
            return;
        }

        Map<String, String> args = CommonHelper.parseFragment(fragment);

        if (args.containsKey("c")) {
            String[] originalCorpusNames = args.get("c").split("\\s*,\\s*");
            Set<String> corpora = new TreeSet<String>(Arrays.asList(originalCorpusNames));

            if (corpora.isEmpty()) {
                if (Helper.getUser(ui) == null && toolbar != null) {
                    // not logged in, show login window
                    boolean onlyCorpusSelected = args.containsKey("c") && args.size() == 1;
                    toolbar.showLoginWindow(!onlyCorpusSelected);
                } else {
                    // already logged in or no login system available, just display a message
                    new Notification("Linked corpus does not exist",
                            "<div><p>The corpus you wanted to access unfortunally does not (yet) exist"
                                    + " in ANNIS.</p>" + "<h2>possible reasons are:</h2>" + "<ul>"
                                    + "<li>that it has not been imported yet,</li>"
                                    + "<li>you don't have the access rights to see this corpus,</li>"
                                    + "<li>or the ANNIS service is not running.</li>" + "</ul>"
                                    + "<p>Please ask the responsible person of the site that contained "
                                    + "the link to import the corpus.</p></div>",
                            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());

                }
            } // end if corpus list returned from service is empty
            else {
                if (args.containsKey("c") && args.size() == 1) {
                    // special case: we were called from outside and should only select,
                    // but not query, the selected corpora
                    ui.getQueryState().getSelectedCorpora().clear();
                    ui.getQueryState().getSelectedCorpora().addAll(corpora);
                } else if (args.get("cl") != null && args.get("cr") != null) {
                    // make sure the properties are not overwritten by the background process
                    getControlPanel().getSearchOptions().setUpdateStateFromConfig(false);

                    DisplayedResultQuery query = QueryGenerator.displayed()
                            .left(Integer.parseInt(args.get("cl")))
                            .right(Integer.parseInt(args.get("cr")))
                            .offset(Integer.parseInt(args.get("s")))
                            .limit(Integer.parseInt(args.get("l"))).segmentation(args.get("seg"))
                            .baseText(args.get("bt")).query(args.get("q")).corpora(corpora).build();

                    if (query.getBaseText() == null && query.getSegmentation() != null) {
                        // if no explicit visible segmentation was given use the same as the context
                        query.setBaseText(query.getSegmentation());
                    }
                    if (query.getBaseText() != null && query.getBaseText().isEmpty()) {
                        // empty string means "null"
                        query.setBaseText(null);
                    }

                    String matchSelectionRaw = args.get("m");
                    if (matchSelectionRaw != null) {
                        for (String selectedMatchNr : Splitter.on(',').omitEmptyStrings()
                                .trimResults().split(matchSelectionRaw)) {
                            try {
                                long nr = Long.parseLong(selectedMatchNr);
                                query.getSelectedMatches().add(nr);
                            } catch (NumberFormatException ex) {
                                log.warn("Invalid long provided as selected match", ex);
                            }
                        }
                    }

                    if (args.get("o") != null) {
                        try {
                          query.setOrder(
                              DisplayedResultQuery.parseOrderFromCitationFragment(args.get("0")));
                        } catch (IllegalArgumentException ex) {
                            log.warn("Could not parse query fragment argument for order", ex);
                        }
                    }

                    if (args.get("ql") != null) {
                        try {
                            query.setQueryLanguage(
                                    QueryLanguage.valueOf(args.get("ql").toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            log.warn(
                                    "Could not parse query fragment argument for the query language",
                                    ex);
                        }
                    }

                    // full query with given context
                    ui.getQueryController().setQuery(query);
                    ui.getQueryController().executeSearch(true, false);
                } else if (args.get("q") != null) {
                    QueryLanguage ql = QueryLanguage.AQL;
                    if (args.get("ql") != null) {
                        try {
                            ql = QueryLanguage.valueOf(args.get("ql").toUpperCase());
                        } catch (IllegalArgumentException ex) {
                            log.warn(
                                    "Could not parse query fragment argument for the query language",
                                    ex);
                        }
                    }
                    // use default context
                    ui.getQueryController().setQuery(new Query(args.get("q"), ql, corpora));
                    ui.getQueryController().executeSearch(true, true);
                }

                getControlPanel().getCorpusList().scrollToSelectedCorpus();

            } // end if corpus list from server was non-empty
        } // end if there is a corpus definition
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public DocBrowserController getDocBrowserController() {
        return docBrowserController;
    }

    public ResultViewPanel getLastSelectedResultView() {
        for (Component c : selectedTabHistory) {
            if (c instanceof ResultViewPanel && mainTab.getTab(c) != null) {
                return (ResultViewPanel) c;
            }
        }
        return null;
    }

    public TabSheet getMainTab() {
        return mainTab;
    }

    public MainToolbar getMainToolbar() {
        return toolbar;
    }

    public TabSheet getTabSheet() {
        return mainTab;
    }

    public void notifiyQueryStarted() {
        if (toolbar != null) {
            toolbar.notifiyQueryStarted();
        }
    }

    @Override
    public void notifyCannotPlayMimeType(String mimeType) {
        if (mimeType == null) {
            return;
        }

        if (mimeType.startsWith("audio/ogg") || mimeType.startsWith("video/web")) {
            String browserList = "<ul>"
                    + "<li>Mozilla Firefox: <a href=\"http://www.mozilla.org/firefox\" target=\"_blank\">http://www.mozilla.org/firefox</a></li>"
                    + "<li>Google Chrome: <a href=\"http://www.google.com/chrome\" target=\"_blank\">http://www.google.com/chrome</a></li>"
                    + "</ul>";

            WebBrowser browser = Page.getCurrent().getWebBrowser();

            // IE9 users can install a plugin
            Set<String> supportedByIE9Plugin = new HashSet<>();
            supportedByIE9Plugin.add("video/webm");
            supportedByIE9Plugin.add("audio/ogg");
            supportedByIE9Plugin.add("video/ogg");

            if (browser.isIE() && browser.getBrowserMajorVersion() >= 9
                    && supportedByIE9Plugin.contains(mimeType)) {
                Notification n = new Notification("Media file type unsupported by your browser",
                        "Please install the WebM plugin for Internet Explorer 9 from "
                                + "<a target=\"_blank\" href=\"https://tools.google.com/dlpage/webmmf\">https://tools.google.com/dlpage/webmmf</a> "
                                + " or use a browser from the following list "
                                + "(these are known to work with WebM or OGG files)<br/>"
                                + browserList
                                + "<br/><br /><strong>Click on this message to hide it</strong>",
                        Notification.Type.WARNING_MESSAGE, true);
                n.setDelayMsec(15000);

                n.show(Page.getCurrent());
            } else {
                Notification n = new Notification("Media file type unsupported by your browser",
                        "Please use a browser from the following list "
                                + "(these are known to work with WebM or OGG files)<br/>"
                                + browserList
                                + "<br/><br /><strong>Click on this message to hide it</strong>",
                        Notification.Type.WARNING_MESSAGE, true);
                n.setDelayMsec(15000);
                n.show(Page.getCurrent());
            }
        } else {
            Notification.show("Media file type \"" + mimeType + "\" unsupported by your browser!",
                    "Try to check your browsers documentation how to enable "
                            + "support for the media type or inform the corpus creator about this problem.",
                    Notification.Type.WARNING_MESSAGE);
        }

    }

    @Override
    public void notifyMightNotPlayMimeType(String mimeType) {
        /*
         * if(!warnedAboutPossibleMediaFormatProblem) { Notification notify = new
         * Notification("Media file type \"" + mimeType +
         * "\" might be unsupported by your browser!",
         * "This means you might get errors playing this file.<br/><br /> " +
         * "<em>If you have problems with this media file:</em><br /> Try to check your browsers " +
         * "documentation how to enable " +
         * "support for the media type or inform the corpus creator about this problem." ,
         * Notification.Type.TRAY_NOTIFICATION, true); notify.setDelayMsec(15000);
         * showNotification(notify); warnedAboutPossibleMediaFormatProblem = true; }
         */
    }

    @Override
    public void onLogin() {
        getControlPanel().getCorpusList().updateCorpusSetList(true);
        // re-evaluate the fragment in case a corpus is now accessible
        evaluateFragment(Page.getCurrent().getUriFragment());
    }

    @Override
    public void onLogout() {
        getControlPanel().getCorpusList().updateCorpusSetList(false);
    }

    @Override
    public void onTabClose(TabSheet tabsheet, Component tabContent) {
        // select the tab that was selected before
        if (tabsheet == mainTab) {
            selectedTabHistory.remove(tabContent);

            if (!selectedTabHistory.isEmpty()) {
                // get the last selected tab
                Component[] asArray =
                        selectedTabHistory.toArray(new Component[selectedTabHistory.size()]);
                mainTab.setSelectedTab(asArray[asArray.length - 1]);
            }
        }

        tabsheet.removeComponent(tabContent);
        if (tabContent instanceof FrequencyQueryPanel) {
            controlPanel.getQueryPanel().notifyFrequencyTabClose();
        }

    }

    @Override
    public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        Component tab = event.getTabSheet().getSelectedTab();
        if (tab != null) {
            // first remove the old element to make sure it is added at the end
            selectedTabHistory.remove(tab);
            selectedTabHistory.add(tab);
        }
    }

    public void setToolbar(MainToolbar newToolbar) {
        // remove old one if necessary
        if (this.toolbar != null) {
            removeComponent(this.toolbar);
            this.toolbar = null;
        }

        // add new toolbar
        if (newToolbar != null) {
            this.toolbar = newToolbar;
            addComponent(this.toolbar, 0, 0, 1, 0);
        }
    }

    /**
     * Adds the _c fragement to the URL in the browser adress bar when a corpus is selected.
     *
     * @param corpora A list of corpora, which are add to the fragment.
     */
    public void updateFragementWithSelectedCorpus(Set<String> corpora) {
        if (corpora != null && !corpora.isEmpty()) {
            String fragment = "_c=" + CommonHelper.encodeBase64URL(StringUtils.join(corpora, ","));
            UI.getCurrent().getPage().setUriFragment(fragment);
        } else {
            UI.getCurrent().getPage().setUriFragment("");
        }
    }

    /**
     * Updates the browser address bar with the current query parameters and the query itself.
     *
     * This is for convenient reloading the vaadin app and easy copying citation links.
     *
     * @param q The query where the parameters are extracted from.
     */
    public void updateFragment(DisplayedResultQuery q) {
        // set our fragment
        lastEvaluatedFragment = q.toCitationFragment();
        UI.getCurrent().getPage().setUriFragment(lastEvaluatedFragment, false);

        // reset title
        Page.getCurrent().setTitle(
                ui.getInstanceConfig().getInstanceDisplayName() + " (ANNIS Corpus Search)");
    }

    @Override
    public void updateSidebarState(SidebarState state) {
        if (controlPanel != null && state != null) {
            controlPanel.setVisible(state.isSidebarVisible());

            // set cookie
            ui.getSettings().set("annis-sidebar-state", state.name(), 30);
        }
    }

    @Override
    public void uriFragmentChanged(Page.UriFragmentChangedEvent event) {
        evaluateFragment(event.getUriFragment());
    }

    public HelpPanel getHelpPanel() {
      return helpPanel;
    }

}
