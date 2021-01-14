/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controller;

import annis.gui.AnnisUI;
import annis.gui.objects.QueryUIState;
import annis.gui.resultview.ResultViewPanel;
import java.util.List;
import java.util.Map;
import org.corpus_tools.annis.ApiCallback;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.model.CountExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CountCallback implements ApiCallback<CountExtra> {
    private static final Logger log = LoggerFactory.getLogger(CountCallback.class);

    private final ResultViewPanel panel;

    private final int pageSize;

    private final AnnisUI ui;

    public CountCallback(ResultViewPanel panel, int pageSize, AnnisUI ui) {
        this.panel = panel;
        this.pageSize = pageSize;
        this.ui = ui;
    }

    @Override
    public void onFailure(ApiException e, int statusCode,
            Map<String, List<String>> responseHeaders) {
        ui.access(() -> {
          ui.getQueryState().getExecutedTasks().remove(QueryUIState.QueryType.COUNT);
          ui.getQueryController().reportServiceException(e, false);
        });

    }

    @Override
    public void onSuccess(CountExtra result, int statusCode,
            Map<String, List<String>> responseHeaders) {
        ui.access(() -> {
            ui.getQueryState().getExecutedTasks().remove(QueryUIState.QueryType.COUNT);

            String documentString = result.getDocumentCount() > 1 ? "documents" : "document";
            String matchesString = result.getMatchCount() > 1 ? "matches" : "match";
            ui.getSearchView().getControlPanel().getQueryPanel()
                    .setStatus("" + result.getMatchCount() + " " + matchesString + "\nin "
                            + result.getDocumentCount() + " " + documentString);
            if (result.getMatchCount() > 0 && panel != null) {
                panel.getPaging().setPageSize(pageSize, false);
                panel.setCount(result.getMatchCount());
            }
            ui.getSearchView().getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
        });
    }

    @Override
    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

    }

    @Override
    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {


    }

}
