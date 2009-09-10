package de.deutschdiachrondigital.dddquery.helper;

import org.junit.Ignore;

@Ignore
public class TestHelpers {

	static int unique = 0;
	
	public static int uniqueInt() {
		return ++unique;
	}

}
