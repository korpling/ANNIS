package mapperV1;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMapperV1Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for mapperV1");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestMapperV1.class);
		//$JUnit-END$
		return suite;
	}

}
