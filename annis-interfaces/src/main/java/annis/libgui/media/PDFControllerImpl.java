/*
 * Copyright 2013 SFB 632.
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
package annis.libgui.media;

import annis.libgui.VisualizationToggle;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class PDFControllerImpl implements PDFController, Serializable
{

  private final Logger log = LoggerFactory.getLogger(PDFControllerImpl.class);

  private Map<String, PDFViewer> registeredPDFViewer;

  private Map<String, VisualizationToggle> registeredVisToggles;

  /**
   * Since everone can call us asynchronously we need a locking mechanism
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  @Override
  public void addPDF(String resultID, PDFViewer pdfViewer)
  {

    lock.writeLock().lock();
    try
    {
      if (registeredPDFViewer == null)
      {
        registeredPDFViewer = new HashMap<String, PDFViewer>();
      }

      if (registeredVisToggles == null)
      {
        registeredVisToggles = new HashMap<String, VisualizationToggle>();
      }

      log.info("registered pdf viewer for result {} -> {}", resultID, pdfViewer);
      registeredPDFViewer.put(resultID, pdfViewer);

    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void openPDF(String resultID, String page)
  {

    lock.readLock().lock();

    try
    {
      if (registeredPDFViewer != null && registeredPDFViewer.containsKey(
        resultID))
      {
        PDFViewer pdfViewer = registeredPDFViewer.get(resultID);
        pdfViewer.openPDF(page);
      }
      else
      {
        log.error("no pdf viewer registered for {}", resultID);
      }
    }
    finally
    {
      lock.readLock().unlock();
    }
  }
}