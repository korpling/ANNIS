package test;




import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


/**
 * Utility code for unit tests.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class TestUtils {

	/**
	 * Generates a random string consisting of 10 characters or numbers. 
	 */
	public static String uniqueString() {
		return RandomStringUtils.randomAlphanumeric(10);
	}
	
	/**
	 * Generates a random string consisting of 10 characters.
	 */
	public static String uniqueAlphaString() {
		return RandomStringUtils.randomAlphabetic(10);
	}

	/**
	 * Generates a random number between 1 and {@link Integer#MAX_VALUE}.
	 */
	public static int uniqueInt() {
		return (int) (Math.random() * Integer.MAX_VALUE);
	}
	
	/**
	 * Generates a random number between 1 and {@link Long#MAX_VALUE}.
	 */
	public static long uniqueLong() {
		return (long) (Math.random() * Long.MAX_VALUE);
	}
	
	/**
	 * Match empty collections
	 */
	@Factory
	public static Matcher<Collection<? extends Object>> empty() {
		return new TypeSafeMatcher<Collection<? extends Object>>() {

			@Override
			public boolean matchesSafely(Collection<? extends Object> item) {
				return item != null && item.isEmpty();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an empty collection");
			}
		};
	}
	
}
