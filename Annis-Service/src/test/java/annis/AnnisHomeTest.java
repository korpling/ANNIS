package annis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

// annis.home sollte nicht im Test, sondern außerhalb gesetzt werden
@Deprecated
@Ignore
public class AnnisHomeTest {

	@BeforeClass
	public static void setupAnnisHomeProperty() {
//		System.out.println(System.getProperty("annis.home"));
//		System.setProperty("annis.home", ".");
//		PropertyConfigurator.configure("./conf/logging.properties");
//		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:MM:ss.SSS} %C{1} %p: %m\n")));
	}

	@AfterClass
	public static void tearDownAnnisHomeProperty() {
//		System.clearProperty("annis.home");
	}
	
}