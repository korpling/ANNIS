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
package annis.visualizers.component.pdf;

import annis.CommonHelper;
import annis.libgui.Helper;
import static annis.libgui.PDFPageHelper.PAGE_NUMBER_SEPERATOR;
import static annis.libgui.PDFPageHelper.PAGE_NO_VALID_NUMBER;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inits the wrapper for the pdf visualization. Neccesary steps for this are:
 * <ul>
 * <li>get the link for the pdf file</li>
 * <li>set the start and end page</li>
 * <li>get a unique id for the wrapper, so pdf.js knows where to create the
 * canvas to.</li>
 * </ul>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@JavaScript({"pdf.js", "pdf_connector.js"})
public class PDFPanel extends AbstractJavaScriptComponent {

  private static final Logger log = LoggerFactory.getLogger(PDFPanel.class);

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
  protected PDFState getState() {
    return (PDFState) super.getState();
  }

  @Override
  public void attach() {
    super.attach();
    setSizeUndefined();

    // set the state
    getState().binaryURL = getBinaryPath();
    getState().pdfID = getPDF_ID();
    getState().firstPage = firstPage;
    getState().lastPage = lastPage;
  }

  private String getBinaryPath() {
    List<String> corpusPath =
            CommonHelper.getCorpusPath(input.getDocument().getSCorpusGraph(),
            input.getDocument());

    String corpusName = corpusPath.get(corpusPath.size() - 1);
    String documentName = corpusPath.get(0);
    try {
      corpusName = URLEncoder.encode(corpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.error("UTF-8 was not known as encoding, expect non-working audio", ex);
    }

    WebResource resMeta = Helper.getAnnisWebResource().path(
            "meta/binary").path(corpusName).path(documentName);
    List<AnnisBinaryMetaData> meta = resMeta.get(
            new GenericType<List<AnnisBinaryMetaData>>() {
    });

    // if there is no document at all don't fail
    String mimeType = meta.size() > 0 ? null : "application/pdf";
    for (AnnisBinaryMetaData m : meta) {
      if (m.getMimeType().equals("application/pdf")) {
        mimeType = m.getMimeType();
        break;
      }
    }

    Validate.notNull(mimeType,
            "There must be at least one binary file for the document with a video mime type");

    String mimeTypeEncoded = mimeType;
    try {
      mimeTypeEncoded = URLEncoder.encode(mimeType, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.error(
              "UTF-8 was not known as encoding, expect strange things will happen",
              ex);
    }

    return input.getContextPath() + "/Binary?"
            + "documentName=" + documentName
            + "&toplevelCorpusName=" + corpusName
            + "&mime=" + mimeTypeEncoded;
  }

  public String getPDF_ID() {
    return PDF_ID;
  }
}
