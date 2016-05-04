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

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;


public class TestAnnisAdminRunner {

	@Mock private CorpusAdministration administration;
	private AnnisAdminRunner main;

	@Before
	public void setup() {
		initMocks(this);

		main = new AnnisAdminRunner();
      main.setCorpusAdministration(null);
		main.setCorpusAdministration(administration);
	}

	@Test
	public void importManyCorpora() {
		run("import data/corpus1 data/corpus2 data/corpus3");

		List<String> expected = Arrays.asList("data/corpus1 data/corpus2 data/corpus3".split(" "));
		verify(administration).importCorporaSave(false, null, null, false, expected);
	}

	@Test
	public void initializeDatabase() {
		run("init -h host --port 5432 -d database -u user -p password");
		verify(administration).initializeDatabase("host", "5432", "database", "user", 
      "password", "postgres", "postgres", null, false, "public");
	}

	@Test
	public void indexes() {
		run("indexes");
		verify(administration).listUsedIndexes();
		verify(administration).listUnusedIndexes();
	}

	private void run(String cmdline) {
		main.run(cmdline.split(" "));
	}

}
