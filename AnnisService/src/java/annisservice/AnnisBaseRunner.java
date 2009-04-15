package annisservice;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

public class AnnisBaseRunner {

	// the root of the Annis installation
	private String annisHomePath;
	
	// run setup code when class is loaded
	{
		checkForAnnisHome();
		setupLogging();
	}
	
	private void checkForAnnisHome() {
		// check if annis.home is set and correct
		annisHomePath = System.getProperty("annis.home");
		if (annisHomePath == null) {
			System.out.println("Please set the annis.home property to the Annis distribution directory.");
			System.exit(1);
		}
		File file = new File(annisHomePath);
		if (! file.exists() || ! file.isDirectory()) {
			System.out.println("The directory '" + annisHomePath + "' does not exist or is not a directory.");
			System.exit(2);
		}
	}
	
	// configure logging
	private void setupLogging() {
		PropertyConfigurator.configure(annisHomePath + "/conf/logging.properties");
	}
	
	///// Getter / Setter
	
	public String getAnnisHome() {
		return annisHomePath;
	}

}
