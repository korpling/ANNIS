/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corpus_tools.annis.gui.frequency;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.AbstractBeanContainer;
import com.vaadin.v7.data.util.DefaultItemSorter;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.ItemSorter;
import com.vaadin.v7.ui.Table;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.corpus_tools.annis.api.model.FrequencyTableRow;
import org.corpus_tools.annis.gui.components.FrequencyChart;
import org.corpus_tools.annis.gui.objects.FrequencyQuery;
import org.corpus_tools.annis.gui.objects.FrequencyTableEntry;
import org.corpus_tools.annis.gui.util.Helper;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class FrequencyResultPanel extends VerticalLayout {
    public static class CSVResource implements StreamResource.StreamSource {
        /**
         * 
         */
        private static final long serialVersionUID = 1793539045259913954L;
        private final List<FrequencyTableRow> data;
        private final List<FrequencyTableEntry> freqDefinition;

        public CSVResource(List<FrequencyTableRow> data, List<FrequencyTableEntry> freqDefinition) {
            this.data = data;
            this.freqDefinition = freqDefinition;
        }

        @Override
        public InputStream getStream() {
            try {
                File tmpFile = File.createTempFile("annis-frequency", ".txt");
                tmpFile.deleteOnExit();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), Charsets.UTF_8);
                        CSVWriter csv = new CSVWriter(writer, '\t', CSVWriter.NO_QUOTE_CHARACTER, '\\')) {

                    // write headers
                    ArrayList<String> header = new ArrayList<>();
                    if (!data.isEmpty()) {
                      for (int i = 0; i < data.iterator().next().getValues().size(); i++) {
                            FrequencyTableEntry e = freqDefinition.get(i);
                            header.add(getCaption(e));
                        }
                    }
                    // add count
                    header.add("count");
                    csv.writeNext(header.toArray(new String[0]));

                    // write entries
                    for (FrequencyTableRow e : data) {
                        ArrayList<String> d = new ArrayList<>();
                        d.addAll(e.getValues());
                        d.add("" + e.getCount());
                        csv.writeNext(d.toArray(new String[0]));
                    }
                }

                return new FileInputStream(tmpFile);

            } catch (IOException ex) {
                log.error(null, ex);
            }
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    public static class IgnoreCaseComparator implements Comparator<Object>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -3584483514353016005L;

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                String s1 = (String) o1;
                String s2 = (String) o2;

                Collator collator = Collator.getInstance(Locale.ENGLISH);
                collator.setStrength(Collator.PRIMARY);
                return collator.compare(s1, s2);
            } else {
                return Ordering.natural().compare((Comparable) o1, (Comparable) o2);
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6481939456466850550L;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FrequencyResultPanel.class);
    public static final int MAX_NUMBER_OF_CHART_ITEMS = 100;

    public static String getCaption(FrequencyTableEntry e) {
        String caption;
        switch (e.getType()) {
        case annotation:
            caption = "#" + e.getReferencedNode() + "|" + e.getKey();
            break;
        case span:
            caption = "#" + e.getReferencedNode() + "|spanned text";
            break;
        default:
            caption = "<unknown>";
        }
        return caption;
    }

    private Table tblResult;
    private final Button btDownloadCSV;

    private final FrequencyChart chart;

    private final FrequencyQueryPanel queryPanel;

    private final FrequencyQuery query;

    public FrequencyResultPanel(List<FrequencyTableRow> table, FrequencyQuery query,
        FrequencyQueryPanel queryPanel) {
        this.query = query;
        this.queryPanel = queryPanel;

        setSizeFull();

        chart = new FrequencyChart(this);
        chart.setHeight("350px");
        chart.setVisible(false);
        addComponent(chart);

        btDownloadCSV = new Button("Download as CSV");
        btDownloadCSV.setDescription("Download as CSV");
        btDownloadCSV.setSizeUndefined();
        addComponent(btDownloadCSV);
        setComponentAlignment(btDownloadCSV, Alignment.TOP_RIGHT);

        btDownloadCSV.setVisible(false);
        btDownloadCSV.setIcon(FontAwesome.DOWNLOAD);
        btDownloadCSV.addStyleName(ValoTheme.BUTTON_SMALL);

        showResult(table);
    }

    private void addLexicalSort(Container container) {
        ItemSorter sorter = new DefaultItemSorter(new IgnoreCaseComparator());

        if (container instanceof IndexedContainer) {
            ((IndexedContainer) container).setItemSorter(sorter);
        } else if (container instanceof AbstractBeanContainer<?, ?>) {
            ((AbstractBeanContainer<?, ?>) container).setItemSorter(sorter);
        }
    }

    private void recreateTable(List<FrequencyTableRow> table) {

        if (tblResult != null) {
            removeComponent(tblResult);
        }

        tblResult = new Table();
        tblResult.setSizeFull();

        tblResult.setCaption(table.size() + " items with a total sum of "
            + table.stream().map(row -> row.getCount()).reduce(0, Integer::sum)
            + " (query on "
                + Joiner.on(", ").join(query.getCorpora()) + ")");

        tblResult.setSelectable(true);
        tblResult.setMultiSelect(false);
        tblResult.addStyleName(Helper.CORPUS_FONT_FORCE);

        if (!table.isEmpty()) {
          FrequencyTableRow firstEntry = table.iterator().next();
          int tupelCount = firstEntry.getValues().size();

            tblResult.addContainerProperty("rank", Integer.class, -1);
            for (int i = 1; i <= tupelCount; i++) {
                tblResult.addContainerProperty("tupel-" + i, String.class, "");
                FrequencyTableEntry e = query.getFrequencyDefinition().get(i - 1);

                tblResult.setColumnHeader("tupel-" + i, getCaption(e));
            }

            tblResult.addContainerProperty("count", Integer.class, -1l);

            int line = 0;
            for (FrequencyTableRow e : table) {
                Object[] cells = new Object[tupelCount + 2];

                System.arraycopy(e.getValues().toArray(), 0, cells, 1, tupelCount);

                cells[0] = line + 1;
                cells[cells.length - 1] = e.getCount();

                tblResult.addItem(cells, "entry-" + line++);
            }
        }
        addLexicalSort(tblResult.getContainerDataSource());

        addComponent(tblResult);
        setExpandRatio(tblResult, 1.0f);

    }

    public void selectRow(int i) {
        tblResult.setValue("entry-" + i);
        tblResult.setCurrentPageFirstItemId("entry-" + i);
    }

    private void showResult(List<FrequencyTableRow> table) {
        if (queryPanel != null) {
            queryPanel.notifiyQueryFinished();
        }
        recreateTable(table);

        btDownloadCSV.setVisible(true);
        FileDownloader downloader = new FileDownloader(
                new StreamResource(new CSVResource(table, query.getFrequencyDefinition()), "frequency.txt"));
        downloader.extend(btDownloadCSV);

        chart.setVisible(true);
        List<FrequencyTableRow> clippedTable = table;
        if (clippedTable.size() > MAX_NUMBER_OF_CHART_ITEMS) {
            clippedTable = new ArrayList<>(table.subList(0, MAX_NUMBER_OF_CHART_ITEMS));
            chart.setCaption("Showing historgram of top " + MAX_NUMBER_OF_CHART_ITEMS
                    + " results, see table below for complete dataset.");
        }
        chart.setFrequencyData(clippedTable);

    }

}
