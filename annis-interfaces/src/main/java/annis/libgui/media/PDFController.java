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
import net.xeoh.plugins.base.Plugin;

/**
 * Offers a annis-gui wide interface for controlling pdf viewer.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface PDFController extends Plugin, Serializable
{

  /**
   * Registers a pdf viewer to the controller.
   *
   * @param resultID The result id is an arbitrary String and does not have to
   * be unique for every pdf viewer. If already a pdf viewer is registered for a
   * specific result id, the new pdf viewer is appended.
   * @param pdfViewer The registered pdf viewer.
   */
  public void addPDF(String resultID, PDFViewer pdfViewer);

  /**
   * Opens a pdf viewer for a specific result id.
   *
   * @param resultID Is an arbitrary String. If no pdf viewer is registered an
   * {@link IllegalArgumentException} is thrown.
   *
   * @param pageNumber Determines the pages of the pdf to render. The String
   * could specify a range (eg. [1-12]) or a singe page (eg. 3).
   *
   */
  public void openPDF(String resultID, String pageNumber);

  /**
   * Informs about the number of already registered pdf viewer.
   *
   * @return the number of registered pdf viewer.
   */
  public int sizeOfRegisterdPDFViewer();
}
