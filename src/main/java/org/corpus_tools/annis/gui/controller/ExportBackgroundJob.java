/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.controller;

import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.components.ExportPanel;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;
import org.corpus_tools.annis.gui.objects.ExportQuery;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ExportBackgroundJob implements Callable<File> {
    private final EventBus eventBus;

    private final ExportPanel panel;

    private final ExportQuery query;

    private final AnnisUI ui;

    private final ExporterPlugin exporter;

    private Exception exportError;

    public ExportBackgroundJob(ExportQuery query, ExporterPlugin exporter, AnnisUI ui,
        EventBus eventBus,
            ExportPanel panel) {
        this.query = query;
        this.eventBus = eventBus;
        this.panel = panel;
        this.ui = ui;
        this.exporter = exporter;
    }

    @Override
    public File call() throws Exception {
        final File currentTmpFile = File.createTempFile("annis-export", "." + exporter.getFileEnding());
        currentTmpFile.deleteOnExit();

        final Map<String, CorpusConfiguration> corpusConfigs = new LinkedHashMap<>();
        for (String c : query.getCorpora()) {
            corpusConfigs.put(c, ui.getCorpusConfigWithCache(c));
        }

        try (final OutputStreamWriter outWriter = new OutputStreamWriter(new FileOutputStream(currentTmpFile),
                "UTF-8")) {

            int leftCtx = exporter.needsContext() ? query.getLeftContext() : 0;
            int rightCtx = exporter.needsContext() ? query.getRightContext() : 0;
            
            org.corpus_tools.annis.api.model.QueryLanguage ql = query.getApiQueryLanguage();
            
            exportError = exporter.convertText(query.getQuery(), ql, leftCtx, rightCtx,
                    query.getCorpora(), query.getAnnotationKeys(), query.getParameters(), query.getAlignmc(),
                outWriter, eventBus, corpusConfigs, ui);
        } finally {
            ui.access(() -> {
                if (panel != null) {
                    panel.showResult(currentTmpFile, exportError);
                }
              if (exportError instanceof WebClientResponseException) {
                ui.getQueryController()
                    .reportServiceException((WebClientResponseException) exportError, true);
                }
            });
        }
        return currentTmpFile;
    }

}
