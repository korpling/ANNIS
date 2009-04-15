/**
 * 
 */
package de.deutschdiachrondigital.dddquery.helper;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class IsCollection extends TypeSafeMatcher<Collection<? extends Object>> {

	Object[] expected;
	
	public IsCollection(Object... expected) {
		this.expected = expected;
	}
	
	@Override
	public boolean matchesSafely(Collection<? extends Object> actual) {
		if (expected.length != actual.size())
			return false;
		for (Object object : expected)
			if ( ! actual.contains(object) )
				return false;
		return true;
	}

	public void describeTo(Description description) {
		description.appendValue(expected);
	}
	
	public static IsCollection isCollection(Object... items) {
		return new IsCollection(items);
	}
	
}