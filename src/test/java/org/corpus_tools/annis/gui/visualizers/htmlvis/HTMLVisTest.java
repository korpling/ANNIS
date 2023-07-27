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
package org.corpus_tools.annis.gui.visualizers.htmlvis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vaadin.server.Page;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.corpus_tools.annis.gui.AnnisBaseUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class HTMLVisTest {

  private HTMLVis fixture;

  static MockWebServer mockServer;
  WebClient client;

  @BeforeAll
  static void setUpClass() throws IOException {
    mockServer = new MockWebServer();
    mockServer.start();
  }

  @AfterAll
  static void tearDownClass() throws IOException {
    mockServer.shutdown();
  }

  @BeforeEach
  public void setup() {

    fixture = new HTMLVis();
    String baseUrl = String.format("http://localhost:%s", mockServer.getPort());
    client = WebClient.create(baseUrl);
  }

  @Test
  void testWebFontInjection() throws WebClientResponseException, IOException {
    AnnisBaseUI ui = mock(AnnisBaseUI.class);

    mockServer.enqueue(new MockResponse().setBody("{\"web-fonts\": [" + "{" + "\"name\": \"FancyFont\", "
        + "\"sources\": {\"format\":\"url\"}}" + "]}")
        .addHeader("Content-Type", "application/json"));

    fixture.injectWebFonts("test", "rootCorpus", "rootCorpus", ui, client);

    verify(ui).injectUniqueCSS(
        "@font-face {\n" + "  font-family: 'FancyFont';\n" + "  font-weight: '400';\n"
            + "  font-style: 'normal';\n" + "  src: url('url') format('format');\n" + "}\n");
  }

  @Test
  void testWebFontInjectionErrorHandling() throws WebClientResponseException, IOException {
    AnnisBaseUI ui = mock(AnnisBaseUI.class);
    Page page = mock(Page.class);

    when(page.getUI()).thenReturn(ui);
    when(ui.getPage()).thenReturn(page);

    mockServer.enqueue(new MockResponse().setBody("{\"web-fonts\"}")
        .addHeader("Content-Type", "application/json"));
    

    fixture.injectWebFonts("test", "rootCorpus", "rootCorpus", ui, client);
    verify(ui, Mockito.never()).injectUniqueCSS(any());

    
    // Don't return a file from the API
    mockServer.enqueue(new MockResponse().setBody("Server error").setResponseCode(500));

    fixture.injectWebFonts("test", "rootCorpus", "rootCorpus", ui, client);

    verify(ui, Mockito.never()).injectUniqueCSS(any());
  }
}
