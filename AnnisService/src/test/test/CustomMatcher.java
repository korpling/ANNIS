package test;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomMatcher {

	public static Matcher<Collection<?>> hasInstance(final Class<?> clazz) {
		return new TypeSafeMatcher<Collection<?>>() {
			
			@Override
			public boolean matchesSafely(Collection<?> collection) {
				for (Object item : collection) {
					if (clazz.isInstance(item))
						return true;
				}
				return false;
			}

			public void describeTo(Description description) {
				description.appendText("a list containing an instance of " + clazz);
			}
			
		};
	}
	
}
