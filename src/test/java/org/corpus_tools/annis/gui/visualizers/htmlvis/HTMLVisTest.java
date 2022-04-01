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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.gui.AnnisBaseUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLVisTest {

  private static final Logger log = LoggerFactory.getLogger(HTMLVisTest.class);
  private HTMLVis fixture;

  @BeforeEach
  public void setup() {

    fixture = new HTMLVis();
  }

  @Test
  public void testWebFontInjection() throws ApiException, IOException {
    AnnisBaseUI ui = mock(AnnisBaseUI.class);
    CorporaApi api = mock(CorporaApi.class);

    File jsonFile = File.createTempFile("test", ".fonts.json");
    FileUtils.writeStringToFile(jsonFile, "{\"web-fonts\": [" + "{" + "\"name\": \"FancyFont\", "
        + "\"sources\": {\"format\":\"url\"}}" + "]}", StandardCharsets.UTF_8);

    when(api.getFile(eq("rootCorpus"), eq("rootCorpus/test.fonts.json"))).thenReturn(jsonFile);

    fixture.injectWebFonts("test", "rootCorpus", "rootCorpus", ui, api);

    verify(ui).injectUniqueCSS(
        eq("@font-face {\n" + "  font-family: 'FancyFont';\n" + "  font-weight: '400';\n"
            + "  font-style: 'normal';\n" + "  src: url('url') format('format');\n" + "}\n"));
  }
}
