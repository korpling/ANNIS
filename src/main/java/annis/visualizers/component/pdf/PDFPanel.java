/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component.pdf;

import static annis.libgui.PDFPageHelper.PAGE_NO_VALID_NUMBER;
import static annis.libgui.PDFPageHelper.PAGE_NUMBER_SEPERATOR;

import annis.gui.components.ExceptionDialog;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import com.google.common.base.Joiner;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;

/**
 * Inits the wrapper for the pdf visualization. Neccesary steps for this are:
 * <ul>
 * <li>get the link for the pdf file</li>
 * <li>set the start and end page</li>
 * <li>get a unique id for the wrapper, so pdf.js knows where to create the canvas to.</li>
 * </ul>
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@JavaScript({"pdf.js", "pdf_connector.js"})
public class PDFPanel extends AbstractJavaScriptComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4956567915350147892L;
  private final static Escaper urlParamEscape = UrlEscapers.urlPathSegmentEscaper();

  private VisualizerInput input;

  private int firstPage;

  private int lastPage;

  private final String PDF_ID;

  public PDFPanel(VisualizerInput input, String page) {

    this.input = input;


    if (!PAGE_NO_VALID_NUMBER.equals(page)) {
      firstPage = Integer.parseInt(page.split(PAGE_NUMBER_SEPERATOR)[0]);
    } else {
      firstPage = Integer.parseInt(PAGE_NO_VALID_NUMBER);
      lastPage = -1;
    }
    // if the last page is not defined, set it to the first page.
    if (page.split(PAGE_NUMBER_SEPERATOR).length > 1) {
      lastPage = Integer.parseInt(page.split(PAGE_NUMBER_SEPERATOR)[1]);
    } else {
      lastPage = firstPage;
    }

    // generate an unique id and set it
    PDF_ID = "pdf-" + UUID.randomUUID();
    setId(PDF_ID);
    addStyleName("pdf-panel");
  }

  @Override
  public void attach() {
    super.attach();
    setSizeUndefined();

    CorporaApi api = new CorporaApi(Helper.getClient(input.getUI()));

    // set the state
    getState().binaryURL = getBinaryPath(api);
    getState().pdfID = getPDF_ID();
    getState().firstPage = firstPage;
    getState().lastPage = lastPage;
  }

  protected String getBinaryPath(CorporaApi api) {
    List<String> corpusPath =
        Helper.getCorpusPath(input.getDocument().getGraph(), input.getDocument());

    Collections.reverse(corpusPath);
    String corpusName = corpusPath.get(0);

    try {
      List<String> files = api.listFiles(corpusName, Joiner.on('/').join(corpusPath));
      for (String f : files) {
        if (f.endsWith(".pdf")) {
          // Create an URL how to featch the PDF file
          return input.getContextPath() + "/Binary?" + "toplevelCorpusName="
              + urlParamEscape.escape(corpusName) + "&file=" + urlParamEscape.escape(f);
        }
      }
    } catch (ApiException e) {
      ExceptionDialog.show(e, input.getUI());
    }

    return "";

  }

  public String getPDF_ID() {
    return PDF_ID;
  }

  @Override
  protected PDFState getState() {
    return (PDFState) super.getState();
  }
}
