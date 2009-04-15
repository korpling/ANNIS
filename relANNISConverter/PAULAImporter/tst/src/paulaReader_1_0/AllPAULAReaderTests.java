package paulaReader_1_0;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPAULAReaderTests {

	public static Test suite() 
	{
		TestSuite suite = new TestSuite("Test for paulaReader_1_0");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestPAULAConnector.class);
		//$JUnit-END$
		return suite;
	}

}
