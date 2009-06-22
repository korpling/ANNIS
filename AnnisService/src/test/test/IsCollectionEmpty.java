/**
 * 
 */
package test;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsCollectionEmpty extends TypeSafeMatcher<Collection<? extends Object>> {
	
	@Override
	public boolean matchesSafely(Collection<? extends Object> collection) {
		return collection.isEmpty();
	}
	
	public void describeTo(Description description) {
		description.appendText("an empty collection");
	}
	
	@Factory
	public static Matcher<Collection<? extends Object>> empty() {
		return new IsCollectionEmpty();
	}
}