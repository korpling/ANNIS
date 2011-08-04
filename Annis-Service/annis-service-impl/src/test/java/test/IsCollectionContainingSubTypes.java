/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * See http://code.google.com/p/hamcrest/issues/detail?id=24
 */
public class IsCollectionContainingSubTypes<T> extends TypeSafeMatcher<Iterable<? super T>> {
	private final Matcher<? extends T> elementMatcher;

	public IsCollectionContainingSubTypes(final Matcher<? extends T> elementMatcher) {
		this.elementMatcher = elementMatcher;
	}

	@Override
	public boolean matchesSafely(final Iterable<? super T> collection) {
		for (final Object item : collection) {
			if (elementMatcher.matches(item)) {
				return true;
			}
		}
		return false;
	}

	public void describeTo(final Description description) {
		description.appendText("a collection containing").appendDescriptionOf(elementMatcher);
	}

	@Factory
	public static <T> Matcher<Iterable<? super T>> containsItem(final Matcher<? extends T> elementMatcher) {
		return new IsCollectionContainingSubTypes<T>(elementMatcher);
	}

	@Factory
	public static <T> Matcher<Iterable<? super T>> containsItem(final T element)	{
		return containsItem(equalTo(element));
	}

	@Factory
	public static <T> Matcher<Iterable<? super T>> containsItems(final Matcher<? extends T>... elementMatchers) {
		final Collection<Matcher<? extends Iterable<? super T>>> all = new ArrayList<Matcher<? extends Iterable<? super T>>>(elementMatchers.length);
		for (final Matcher<? extends T> elementMatcher : elementMatchers) {
			all.add(containsItem(elementMatcher));
		}
		return allOf(all);
	}

	@Factory
	public static <T> Matcher<Iterable<? super T>> containsItems(final T... elements) {
		final Collection<Matcher<? extends Iterable<? super T>>> all = new ArrayList<Matcher<? extends Iterable<? super T>>>(elements.length);
		for (final T element : elements) {
			all.add(containsItem(element));
		}
		return allOf(all);
	}
	
	@Factory 
	public static <T> Matcher<Iterable<? super T>> containsItems(final Collection<? extends T> elements) {
		final Collection<Matcher<? extends Iterable<? super T>>> all = new ArrayList<Matcher<? extends Iterable<? super T>>>(elements.size());
		for (final T element : elements) {
			all.add(containsItem(element));
		}
		return allOf(all);
	}
	
}
