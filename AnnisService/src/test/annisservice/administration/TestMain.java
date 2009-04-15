package annisservice.administration;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations.Mock;


@SuppressWarnings("static-access")
public class TestMain {
	
	@Mock private CorpusAdministration administration;
	private AnnisAdminRunner main;
	
	@Before
	public void setup() {
		initMocks(this);

		main = new AnnisAdminRunner(administration);
	}

	@Test
	public void importManyCorpora() {
		run("import data/corpus1 data/corpus2 data/corpus3");

		List<String> expected = Arrays.asList("data/corpus1 data/corpus2 data/corpus3".split(" "));
		verify(administration).importCorpora(expected);
	}
	
	@Test
	public void initializeDatabase() {
		run("init -h host --port 5432 -d database -u user -p password");
		verify(administration).initializeDatabase("host", "5432", "database", "user", "password", "postgres", "postgres", null);
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
