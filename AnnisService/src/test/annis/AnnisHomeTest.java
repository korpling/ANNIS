package annis;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AnnisHomeTest {

	@BeforeClass
	public static void setupAnnisHomeProperty() {
		System.setProperty("annis.home", ".");
		PropertyConfigurator.configure("./conf/logging.properties");
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:MM:ss.SSS} %C{1} %p: %m\n")));
	}

	@AfterClass
	public static void tearDownAnnisHomeProperty() {
		System.clearProperty("annis.home");
	}
	
}