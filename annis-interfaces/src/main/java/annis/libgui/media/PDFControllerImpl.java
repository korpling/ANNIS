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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@SuppressWarnings("serial")
public class PDFControllerImpl implements PDFController, Serializable
{

  private static final Logger log = LoggerFactory.getLogger(PDFControllerImpl.class);

  private transient Map<String, List<PDFViewer>> registeredPDFViewer;

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
        registeredPDFViewer = new HashMap<String, List<PDFViewer>>();
      }

      if (!registeredPDFViewer.containsKey(resultID))
      {
        List<PDFViewer> pdfViewers = new ArrayList<PDFViewer>();
        registeredPDFViewer.put(resultID, pdfViewers);
      }

      registeredPDFViewer.get(resultID).add(pdfViewer);
      log.info("registered pdf viewer for result {} -> {}", resultID,
        pdfViewer);

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
        for (PDFViewer pdfViewer : registeredPDFViewer.get(resultID))
        {
          pdfViewer.openPDFPage(page);
        }
      }
      else
      {
        throw new IllegalArgumentException(
          "no pdf registered for this id: " + resultID);
      }
    }
    finally
    {
      lock.readLock().unlock();
    }
  }

  @Override
  public int sizeOfRegisterdPDFViewer()
  {
    return registeredPDFViewer != null ? registeredPDFViewer.size() : 0;
  }
}