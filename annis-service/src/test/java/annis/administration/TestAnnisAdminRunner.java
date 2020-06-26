/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.administration;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TestAnnisAdminRunner {

    @Mock
    private CorpusAdministration administration;
    private AnnisAdminRunner main;

    @Test
    public void importManyCorpora() throws InterruptedException {
        run("import data/corpus1 data/corpus2 data/corpus3");

        List<String> expected = Arrays.asList("data/corpus1 data/corpus2 data/corpus3".split(" "));
        verify(administration).importCorporaSave(false, false, null, null, false, expected);
    }

    private void run(String cmdline) throws InterruptedException {
        main.run(cmdline.split(" "));
    }

    @Before
    public void setup() throws GraphANNISException {
        initMocks(this);

        main = new AnnisAdminRunner();
        main.setCorpusAdministration(null);
        main.setCorpusAdministration(administration);
    }

}
