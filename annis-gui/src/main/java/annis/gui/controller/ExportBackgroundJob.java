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
package annis.gui.controller;

import annis.gui.AnnisUI;
import annis.gui.ExportPanel;
import annis.gui.exporter.Exporter;
import annis.gui.objects.ExportQuery;
import annis.libgui.Helper;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ExportBackgroundJob implements Callable<File>
{
  private final EventBus eventBus;

  private final ExportPanel panel;

  private final ExportQuery query;

  private final AnnisUI ui;

  private final Exporter exporter;
  
  private Exception exportError;

  public ExportBackgroundJob(ExportQuery query, Exporter exporter, AnnisUI ui,
    EventBus eventBus, ExportPanel panel)
  {
    this.query = query;
    this.eventBus = eventBus;
    this.panel = panel;
    this.ui = ui;
    this.exporter = exporter;
  }

  @Override
  public File call() throws Exception
  {
    final File currentTmpFile = File.createTempFile("annis-export", ".txt");
    currentTmpFile.deleteOnExit();
    try (final OutputStreamWriter outWriter = new OutputStreamWriter(new FileOutputStream(currentTmpFile),
      "UTF-8"))
    {
      exportError = exporter.convertText(query.getQuery(), query.getLeftContext(),
        query.getRightContext(), query.getCorpora(), query.getAnnotationKeys(),
        query.getParameters(), Helper.getAnnisWebResource().path("query"),
        outWriter, eventBus);
    }
    finally
    {
      ui.access(new Runnable()
      {
        @Override
        public void run()
        {
          if (panel != null)
          {
            panel.showResult(currentTmpFile, exportError instanceof InterruptedException);
          }
          if(exportError instanceof UniformInterfaceException)
          {
            ui.getQueryController().reportServiceException((UniformInterfaceException) exportError, true);
          }
        }
      });
    }
    return currentTmpFile;
  }
  
}
