
/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.flatquerybuilder;

import annis.gui.QueryController;
import annis.gui.objects.QueryUIState;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.model.Query;
import annis.service.objects.QueryLanguage;
import com.vaadin.data.Binder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.NativeSelect;
import com.vaadin.v7.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author martin klotz (martin.klotz@hu-berlin.de)
 * 
 * @author tom ruette (tom.ruette@hu-berlin.de)
 */
public class FlatQueryBuilder extends Panel implements Button.ClickListener {

    /**
     * 
     */
    private static final long serialVersionUID = -1659782316940380300L;
    private static final Logger log = LoggerFactory.getLogger(FlatQueryBuilder.class);

    private static final String[] REGEX_CHARACTERS =
            {"\\", "+", ".", "[", "*", "^", "$", "|", "?", "(", ")"};
    private static final String BUTTON_GO_LABEL = "Create AQL Query";
    private static final String BUTTON_CLEAR_LABEL = "Clear the Query Builder";
    private static final String NO_CORPORA_WARNING =
            "No corpora selected, please select " + "at least one corpus.";
    private static final String INCOMPLETE_QUERY_WARNING = "Query seems to be incomplete.";
    private static final String ADD_LING_PARAM = "Add";
    private static final String ADD_SPAN_PARAM = "Add";
    private static final String CHANGE_SPAN_PARAM = "Change";
    private static final String ADD_META_PARAM = "Add";
    private static final String INFO_INIT_LANG = "In this part of the Query Builder, "
            + "blocks of the linguistic query can be constructed from left to right.";
    private static final String INFO_INIT_SPAN = "This part of the Query Builder "
            + "allows you to define a span annotation within which the above query blocks "
            + "are confined.";
    private static final String INFO_INIT_META =
            "Here, you can constrain the linguistic " + "query by selecting meta levels.";
    private static final String INFO_FILTER = "When searching in the fields, the "
            + "hits are sorted and filtered according to different mechanisms. Please "
            + "choose a filtering mechanism here.";
    private static final String TOOLBAR_CAPTION = "Toolbar";
    private static final String META_CAPTION = "Meta information";

    private static final String SPAN_CAPTION = "Scope";

    private static final String LANG_CAPTION = "Linguistic sequence";
    private static final String ADVANCED_CAPTION = "Advanced settings";
    private Button btGo;
    private Button btClear;

    private Button btInitSpan;
    private Button btInitMeta;
    private Button btInitLanguage;
    private MenuBar addMenu;

    private MenuBar addMenuSpan;
    private MenuBar addMenuMeta;
    private QueryController cp;
    private HorizontalLayout language;

    private HorizontalLayout languagenodes;
    private HorizontalLayout span;
    private HorizontalLayout meta;
    private HorizontalLayout toolbar;
    private HorizontalLayout advanced;

    private VerticalLayout mainLayout;

    private NativeSelect filtering;

    private Collection<VerticalNode> vnodes;

    private Collection<EdgeBox> eboxes;

    private Collection<MetaBox> mboxes;

    private SpanBox spbox;

    private MenuBar.MenuItem spanMenu;

    private ReducingStringComparator rsc;

    public FlatQueryBuilder(QueryController cp) {
        setSizeFull();
        launch(cp);

    }

    public void addLinguisticSequenceBox(String annoName) {
      if (!vnodes.isEmpty()) {
        EdgeBox eb = new EdgeBox(this);
        languagenodes.addComponent(eb);
        eboxes.add(eb);
      }
      VerticalNode vn = new VerticalNode(annoName, this);
      languagenodes.addComponent(vn);
      vnodes.add(vn);
      addMenu.setAutoOpen(false);
    }


    void addSpanBox(String level) {
        spbox = new SpanBox(level, this);
        span.addComponent(spbox);
        span.setComponentAlignment(spbox, Alignment.MIDDLE_LEFT);
        spanMenu.setText(CHANGE_SPAN_PARAM);
    }

    void addMetaBox(String annoname) {
      MetaBox mb = new MetaBox(annoname, this);
      meta.addComponent(mb);
      mboxes.add(mb);
      addMenuMeta.setAutoOpen(false);
      for(MenuItem textItem : addMenuMeta.getItems()) {
        if ("Add".equals(textItem.getText())) {
          for (MenuItem item : textItem.getChildren()) {
            if (annoname.equals(item.getText())) {
              item.setVisible(false);
            }
          }
        }
      }
    }

    @Override
    public void attach() {
        super.attach();

        Binder<QueryUIState> binder = new Binder<>();
        binder.addValueChangeListener(e -> this.initialize());
        binder.setBean(cp.getState());

        IDGenerator.assignIDForFields(this, language, btInitLanguage, btGo);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (cp.getState().getSelectedCorpora().isEmpty()) {
            Notification.show(NO_CORPORA_WARNING);
        } else {
            if (event.getButton() == btGo) {
                updateQuery();
            }

            if (event.getButton() == btClear) {
                clear();
                updateQuery();
                launch(cp);
            }

            if (event.getComponent() == btInitMeta || event.getComponent() == btInitSpan
                    || event.getComponent() == btInitLanguage) {
                initialize();
            }
        }
    }

    private void clear()
    // check whether it is necessary to do this in a method
    {
        language.removeAllComponents();
        span.removeAllComponents();
        meta.removeAllComponents();
        toolbar.removeAllComponents();
        mainLayout.removeComponent(language);
        mainLayout.removeComponent(span);
        mainLayout.removeComponent(meta);
        mainLayout.removeComponent(toolbar);
        vnodes.clear();
        eboxes.clear();
        mboxes.clear();
    }

    public String escapeRegexCharacters(String tok) {
        if (tok == null) {
            return "";
        }
        if (tok.equals("")) {
            return "";
        }
        String result = tok;
        for (int i = 0; i < REGEX_CHARACTERS.length; i++) {
            result = result.replace(REGEX_CHARACTERS[i], "\\" + REGEX_CHARACTERS[i]);
        }

        return result.replace("/", "\\x2F");
    }

    public Collection<String> getAnnotationValues(String level) {
        Collection<String> values = new TreeSet<>();
        for (String s : getAvailableAnnotationLevels(level)) {
            values.add(s);
        }
        return values;
    }

    private String getAQLFragment(SearchBox sb) {
        String result = "";
        String value = null;
        try {
            value = sb.getValue();
        } catch (java.lang.NullPointerException ex) {
            value = null;
        }
        String level = sb.getAttribute();
        if (value == null) {
            result = level;
        }
        if (value != null) {
            if (sb.isRegEx() && !sb.isNegativeSearch()) {
                result = level + "=/" + value.replace("/", "\\x2F") + "/";
            }
            if (sb.isRegEx() && sb.isNegativeSearch()) {
                result = level + "!=/" + value.replace("/", "\\x2F") + "/";
            }
            if (!sb.isRegEx() && sb.isNegativeSearch()) {
                result = level + "!=\"" + value.replace("\"", "\\x22") + "\"";
            }
            if (!sb.isRegEx() && !sb.isNegativeSearch()) {
                result = level + "=\"" + value.replace("\"", "\\x22") + "\"";
            }
        }
        return result;
    }

    private String getAQLQuery() {
        int count = 1;
        StringBuilder ql = new StringBuilder();
        StringBuilder edgeQuery = new StringBuilder();
        StringBuilder sentenceQuery = new StringBuilder();
        Collection<Integer> sentenceVars = new ArrayList<>();
        Iterator<EdgeBox> itEboxes = eboxes.iterator();
        for (VerticalNode v : vnodes) {
            Collection<SearchBox> sboxes = v.getSearchBoxes();
            for (SearchBox s : sboxes) {
                ql.append(" & " + getAQLFragment(s));
            }
            if (sboxes.isEmpty()) {
                // not sure we want to do it this way:
                ql.append("\n& /.*/");
            }
            sentenceVars.add(Integer.valueOf(count));
            for (int i = 1; i < sboxes.size(); i++) {
                String addQuery = "\n& #" + count + "_=_" + "#" + ++count;
                edgeQuery.append(addQuery);
            }
            count++;
            String edgeQueryAdds = (itEboxes.hasNext())
                    ? "\n& #" + (count - 1) + " " + itEboxes.next().getValue() + " #" + count
                    : "";
            edgeQuery.append(edgeQueryAdds);
        }
        String addQuery = "";
        try {
            SpanBox spb = (SpanBox) span.getComponent(1);
            if ((!spb.isRegEx()) && (!spb.getValue().isEmpty())) {
                addQuery = "\n& " + spb.getAttribute() + " = \"" + spb.getValue() + "\"";
            }
            if (spb.isRegEx()) {
                addQuery = "\n& " + spb.getAttribute() + " = /"
                        + spb.getValue().replace("/", "\\x2F") + "/";
            }
            if (spb.getValue().isEmpty()) {
                addQuery = "\n&" + spb.getAttribute();
            }
            ql.append(addQuery);
            for (Integer i : sentenceVars) {
                sentenceQuery.append("\n& #").append(count).append("_i_#").append(i.toString());
            }
        } catch (Exception ex) {
          // Ignore this exception
        }
        StringBuilder metaQuery = new StringBuilder();
        Iterator<MetaBox> itMetaBoxes = mboxes.iterator();
        while (itMetaBoxes.hasNext()) {
            metaQuery.append(getMetaQueryFragment(itMetaBoxes.next()));
        }
        String fullQuery = (ql.toString() + edgeQuery.toString() + sentenceQuery.toString()
                + metaQuery.toString());
        if (fullQuery.length() < 3) {
            return "";
        }
        fullQuery = fullQuery.substring(3);// deletes leading " & "
        return fullQuery;
    }

    public Collection<String> getAvailableAnnotationLevels(String meta) {
        Collection<String> result = new TreeSet<>();
        CorporaApi api = new CorporaApi(Helper.getClient(UI.getCurrent()));
        // get current corpus selection
        Collection<String> corpusSelection = cp.getState().getSelectedCorpora();
        try {
            List<Annotation> atts = new LinkedList<>();
            for (String corpus : corpusSelection) {
                atts.addAll(api.nodeAnnotations(corpus, true, false));
            }
            for (Annotation a : atts) {
                if (a.getKey().getName().equals(meta)) {
                    result.add(a.getVal());
                }
            }
        } catch (ApiException ex) {
            log.error(null, ex);
        }

        return result;
    }

    public Set<String> getAvailableAnnotationNames() {
        Set<String> result = new TreeSet<>();
        // get current corpus selection
        Collection<String> corpusSelection = cp.getState().getSelectedCorpora();
        CorporaApi api = new CorporaApi(Helper.getClient(UI.getCurrent()));
        try {
            for (String corpus : corpusSelection) {
                for (Annotation a : api.nodeAnnotations(corpus, false, false)) {
                    result.add(a.getKey().getName());
                }
            }
        } catch (ApiException ex) {
            log.error(null, ex);
        }
        result.add("tok");
        return result;
    }

    public Set<String> getAvailableMetaNames() {
        Set<String> result = new TreeSet<>();
        // get current corpus selection
        Collection<String> corpusSelection = cp.getState().getSelectedCorpora();
        try {
            for (String corpus : corpusSelection) {
                for (AnnoKey key : Helper.getMetaAnnotationNames(corpus, UI.getCurrent())) {
                    result.add(key.getName());
                }
            }

        } catch (ApiException ex) {
            log.error(null, ex);
        }

        return result;
    }

    public String getFilterMechanism() {
        return filtering.getItemCaption(filtering.getValue());
    }

    private String getMetaQueryFragment(MetaBox mb) {
        Collection<String> values = mb.getValues();
        if (!values.isEmpty()) {
            StringBuilder result = new StringBuilder("\n& meta::" + mb.getMetaDatum() + " = ");
            if (values.size() == 1) {
                result.append("\"" + values.iterator().next().replace("\"", "\\x22") + "\"");
            } else {
                Iterator<String> itValues = values.iterator();
                result.append("/(" + escapeRegexCharacters(itValues.next()) + ")");
                while (itValues.hasNext()) {
                    result.append("|(" + escapeRegexCharacters(itValues.next()) + ")");
                }
                result.append("/");
            }
            return result.toString();
        }
        return "";
    }

    public ReducingStringComparator getRSC() {
        return rsc;
    }

    private void initialize() {
        // try to remove all existing menus
        try {
          if (addMenu != null) {
            language.removeComponent(addMenu);
          }
          if (addMenuSpan != null) {
            span.removeComponent(addMenuSpan);
          }
          if (addMenuMeta != null) {
            meta.removeComponent(addMenuMeta);
          }
        } catch (Exception e) {
            log.error(null, e);
        }

        // init variables:
        final FlatQueryBuilder sq = this;
        Collection<String> annonames = getAvailableAnnotationNames();
        Collection<String> metanames = getAvailableMetaNames();

        // Code from btInitLanguage:
        addMenu = new MenuBar();
        // addMenu.setDescription(INFO_INIT_LANG);
        addMenu.setAutoOpen(false);
        final MenuBar.MenuItem add = addMenu.addItem(ADD_LING_PARAM, null);
        for (final String annoname : annonames) {
          add.addItem(annoname, selectedItem -> addLinguisticSequenceBox(annoname));
        }
        language.removeComponent(btInitLanguage);
        language.addComponent(addMenu);

        // Code from btInitSpan:
        addMenuSpan = new MenuBar();
        // addMenuSpan.setDescription(INFO_INIT_SPAN);
        addMenuSpan.setAutoOpen(false);
        final MenuBar.MenuItem addSpan = addMenuSpan.addItem(ADD_SPAN_PARAM, null);
        for (final String annoname : annonames) {
            addSpan.addItem(annoname, selectedItem -> {
                sq.removeSpanBox();
                sq.addSpanBox(annoname);
                addMenuSpan.setAutoOpen(false);
            });
        }
        spanMenu = addSpan;
        span.removeComponent(btInitSpan);
        span.addComponent(addMenuSpan);

        // Code from btInitMeta:
        addMenuMeta = new MenuBar();
        // addMenuMeta.setDescription(INFO_INIT_META);
        addMenuMeta.setAutoOpen(false);
        final MenuBar.MenuItem addMeta = addMenuMeta.addItem(ADD_META_PARAM, null);
        for (final String annoname : metanames) {
          addMeta.addItem(annoname, selectedItem -> addMetaBox(annoname));
        }
        meta.removeComponent(btInitMeta);
        meta.addComponent(addMenuMeta);
    }

    private void launch(QueryController cp) {
        this.cp = cp;
        rsc = new ReducingStringComparator();
        mainLayout = new VerticalLayout();
        // tracking lists for vertical nodes, edgeboxes and metaboxes
        vnodes = new ArrayList<>();
        eboxes = new ArrayList<>();
        mboxes = new ArrayList<>();
        spbox = null;
        // buttons and checks
        btGo = new Button(BUTTON_GO_LABEL, this);
        btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);
        btClear = new Button(BUTTON_CLEAR_LABEL, this);
        btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);
        btInitLanguage = new Button("Initialize", this);
        btInitLanguage.setDescription(INFO_INIT_LANG);
        btInitSpan = new Button("Initialize", this);
        btInitSpan.setDescription(INFO_INIT_SPAN);
        btInitMeta = new Button("Initialize", this);
        btInitMeta.setDescription(INFO_INIT_META);
        filtering = new NativeSelect("Filtering mechanisms");
        filtering.setDescription(INFO_FILTER);
        ReducingStringComparator rdc = new ReducingStringComparator();
        Set mappings = rdc.getMappings().keySet();
        int i;
        for (i = 0; i < mappings.size(); i++) {
            String mapname = (String) mappings.toArray()[i];
            filtering.addItem(i);
            filtering.setItemCaption(i, mapname);
        }
        filtering.addItem(i + 1);
        filtering.setItemCaption(i + 1, "generic");
        filtering.select(i + 1);
        filtering.setNullSelectionAllowed(false);
        filtering.setImmediate(true);
        // language layout
        language = new HorizontalLayout();
        languagenodes = new HorizontalLayout();
        language.addComponent(languagenodes);
        language.addComponent(btInitLanguage);
        language.setMargin(true);
        language.setCaption(LANG_CAPTION);
        language.addStyleName("linguistics-panel");
        // span layout
        span = new HorizontalLayout();
        span.setSpacing(true);
        span.addComponent(btInitSpan);
        span.setMargin(true);
        span.setCaption(SPAN_CAPTION);
        span.addStyleName("span-panel");
        // meta layout
        meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.addComponent(btInitMeta);
        meta.setMargin(true);
        meta.setCaption(META_CAPTION);
        meta.addStyleName("meta-panel");
        // toolbar layout
        toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.addComponent(btGo);
        toolbar.addComponent(btClear);
        toolbar.setMargin(true);
        toolbar.setCaption(TOOLBAR_CAPTION);
        toolbar.addStyleName("toolbar-panel");
        // advanced
        advanced = new HorizontalLayout();
        advanced.setSpacing(true);
        advanced.addComponent(filtering);
        advanced.setMargin(true);
        advanced.setCaption(ADVANCED_CAPTION);
        advanced.addStyleName("advanced-panel");
        // put everything on the layout
        mainLayout.setSpacing(true);
        mainLayout.addComponent(language);
        mainLayout.addComponent(span);
        mainLayout.addComponent(meta);
        mainLayout.addComponent(toolbar);
        mainLayout.addComponent(advanced);
        setContent(mainLayout);
        getContent().setWidth("100%");
        getContent().setHeight("-1px");
    }

    public void removeMetaBox(MetaBox v) {
        meta.removeComponent(v);
        mboxes.remove(v);
        List<MenuBar.MenuItem> items = addMenuMeta.getItems().get(0).getChildren();
        boolean found = false;
        String metalevel = v.getMetaDatum();
        for (int i = 0; (i < items.size()) && !found; i++) {
            MenuBar.MenuItem itm = items.get(i);
            if (itm.getText().equals(metalevel)) {
                itm.setVisible(true);
                found = true;
            }
        }
    }

    public void removeSpanBox() {
        if (spbox != null) {
            span.removeComponent(spbox);
            spbox = null;
            spanMenu.setText(ADD_SPAN_PARAM);
        }
    }

    public void removeSpanBox(SpanBox spb) {
        if (spb.equals(spbox)) {
            removeSpanBox();
        }
    }

    public void removeVerticalNode(VerticalNode v) {
        Iterator<VerticalNode> itVnodes = vnodes.iterator();
        Iterator<EdgeBox> itEboxes = eboxes.iterator();
        VerticalNode vn = itVnodes.next();
        EdgeBox eb = null;

        while (!vn.equals(v)) {
            vn = itVnodes.next();
            eb = itEboxes.next();
        }

        if ((eb == null) && (itEboxes.hasNext())) {
            eb = itEboxes.next();
        }

        vnodes.remove(v);
        if (eb != null) {
            eboxes.remove(eb);
            languagenodes.removeComponent(eb);
        }
        languagenodes.removeComponent(v);
    }


    public String unescape(String s) {
        // first unescape slashes and quotes:

        s = unescapeSlQ(s);

        // unescape regex characters:
        int i = 1;
        while (i < s.length()) {
            char c0 = s.charAt(i - 1);
            char c1 = s.charAt(i);
            for (int j = 0; j < REGEX_CHARACTERS.length; j++) {
                if ((c1 == REGEX_CHARACTERS[j].charAt(0)) && (c0 == '\\')) {
                    s = s.substring(0, i - 1) + s.substring(i);
                    break;
                }
                if (j == REGEX_CHARACTERS.length - 1) {
                    i++;
                }
            }
        }

        return s;
    }

    public String unescapeSlQ(String s) {
        return s.replace("\\x2F", "/").replace("\\x22", "\"");
    }

    public void updateQuery() {
        try {
            cp.setQuery(new Query(getAQLQuery(), QueryLanguage.AQL,
                    new LinkedHashSet<>(cp.getState().getSelectedCorpora())));
        } catch (java.lang.NullPointerException ex) {
            Notification.show(INCOMPLETE_QUERY_WARNING);
        }
    }
}
