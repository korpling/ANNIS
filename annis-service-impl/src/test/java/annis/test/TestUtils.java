package annis.test;




import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;


/**
 * Utility code for unit tests.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class TestUtils {

  
  // FIXME: either rename the class or exclude it from maven testing
  @Test public void dummyTest() { }
  
  /**
   * Create a set from a list of items.

   * @param items Items that should be added to the set.
   * @return A {@link HashSet} containing the specified items.
   */
  public static <T> Set<T> newSet(T... items) {
    Set<T> set = new HashSet<T>();
    for (T item : items) {
      set.add(item);
    }
    return set;
  }
  
  
  /**
   * Create an empty set.
   * 
   * @param clazz The type of class which should be contained in the set.
   * @return An empty typed set.
   */
  public static <T> Set<T> emptySetOf(Class<T> clazz) {
    Set<T> set = new HashSet<T>();
    return set;
  }

  /**
   * Generates a random string consisting of 10 characters or numbers. 
   */
  public static String uniqueString() {
    return uniqueString(10);
  }
  
  /**
   * Generates a random string consisting of {@code length} characters or numbers.
   * @param length Length of the returned string.
   */
  public static String uniqueString(int length) {
    return RandomStringUtils.randomAlphanumeric(length);
  }
  
  /**
   * Generates a random string consisting of 10 characters.
   */
  public static String uniqueAlphaString() {
    return RandomStringUtils.randomAlphabetic(10);
  }

  /**
   * Generates a random number between 0 and {@link Integer#MAX_VALUE}.
   */
  public static int uniqueInt() {
    return uniqueInt(Integer.MAX_VALUE);
  }
  
  /**
   * Generates a random number between 0 and {@code maxValue}.
   * @param maxValue The maximum returned value.
   */
  public static int uniqueInt(int maxValue) {
    return uniqueInt(0, maxValue);
  }
  
  /**
   * Generates a random number between {@code minValue} and {@code maxValue}.
   * @param minValue The minimum returned value.
   * @param maxValue The maximum returned value.
   */
  public static int uniqueInt(int minValue, int maxValue) {
    Validate.isTrue(maxValue > minValue, "maxValue (" + maxValue + ") is not larger than minValue (" + minValue + ")");
    return minValue + (int) (Math.random() * (maxValue - minValue)); 
  }
  
  /**
   * Generates a random number between 1 and {@link Long#MAX_VALUE}.
   */
  public static long uniqueLong() {
    return (long) (Math.random() * Long.MAX_VALUE);
  }
  
  /**
   * Test for empty collections.
   * 
   * @return A Hamcrest matcher that matches empty collections.
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

  /**
   * Test the size of collections.
   *
   * @param size The expected size of the collection.
   * @return A Hamcrest matcher that matches the size of a collection.
   */
  @Factory
    public static Matcher<Collection<? extends Object>> size(final int size) {
    return new TypeSafeMatcher<Collection<? extends Object>>() {
      
      private int actual;
      
      @Override
      public boolean matchesSafely(Collection<? extends Object> collection) {
        actual = collection.size();
        return collection.size() == size;
      }
      
      public void describeTo(Description description) {
        description.appendText("a collection with " + size + " element" + (size == 1 ? "" : "s") + "; got: " + actual);
      }
    };
  }
    
}
