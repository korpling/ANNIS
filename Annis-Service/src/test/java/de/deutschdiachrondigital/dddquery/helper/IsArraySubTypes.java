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
package de.deutschdiachrondigital.dddquery.helper;

import java.util.Arrays;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsArraySubTypes<T> extends TypeSafeMatcher<Iterable<? super T>> {
    private final Matcher<? super T>[] elementMatchers;
    
    public IsArraySubTypes(Matcher<? super T>[] elementMatchers) {
        this.elementMatchers = elementMatchers.clone();
    }
    
	@Override
	public boolean matchesSafely(Iterable<? super T> array) {
//        if (array.length != elementMatchers.length) return false;
//        
//        for (int i = 0; i < array.length; i++) {
//            if (!elementMatchers[i].matches(array[i])) return false;
//        }
//        
        return true;
    }
    
    public void describeTo(Description description) {
        description.appendList(descriptionStart(), descriptionSeparator(), descriptionEnd(), 
                               Arrays.asList(elementMatchers));
    }
    
    /**
     * Returns the string that starts the description.
     * 
     * Can be overridden in subclasses to customise how the matcher is
     * described.
     */
    protected String descriptionStart() {
        return "[";
    }

    /**
     * Returns the string that separates the elements in the description.
     * 
     * Can be overridden in subclasses to customise how the matcher is
     * described.
     */
    protected String descriptionSeparator() {
        return ", ";
    }

    /**
     * Returns the string that ends the description.
     * 
     * Can be overridden in subclasses to customise how the matcher is
     * described.
     */
    protected String descriptionEnd() {
        return "]";
    }
    
    public static <T> IsArraySubTypes<Iterable<? super T>> array(Matcher<T>... elementMatchers) {
        return null; // new IsArraySubTypes<Iterable<? super T>>(elementMatchers);
    }

}
