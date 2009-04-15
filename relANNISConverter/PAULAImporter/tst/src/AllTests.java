import mapperV1.AllMapperV1Tests;
import paulaReader_1_0.AllPAULAReaderTests;
import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for PAULAImporter");
		//$JUnit-BEGIN$
		suite.addTest(AllPAULAReaderTests.suite());
		suite.addTest(AllMapperV1Tests.suite());
		//$JUnit-END$
		return suite;
	}

}
