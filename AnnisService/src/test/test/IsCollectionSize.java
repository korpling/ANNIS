/**
 * 
 */
package test;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsCollectionSize extends TypeSafeMatcher<Collection<? extends Object>> {
	
	private int expected;
	private int actual;
	
	public IsCollectionSize(int expected) {
		this.expected = expected;
	}
	
	@Override
	public boolean matchesSafely(Collection<? extends Object> collection) {
		actual = collection.size();
		return collection.size() == expected;
	}
	
	public void describeTo(Description description) {
		description.appendText("a collection with " + expected + " element" + (expected == 1 ? "" : "s") + "; got: " + actual);
	}
	
	@Factory
	public static Matcher<Collection<? extends Object>> size(int size) {
		return new IsCollectionSize(size);
	}
	
}