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

/**
 * Provides a pdf viewer for the annis gui and makes it controllable for all
 * components of the gui.
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface PDFViewer
{

  /**
   * Opens a pdf visualizer and renders a specific page range
   *
   * @param page Determines the pages of the pdf to render. The String could
   * specify a range (eg. [1-12]) or a singe page (eg. 3).
   *
   */
  public void openPDFPage(String page);
}
