/**
 * 
 */
package test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.experimental.theories.ParametersSuppliedBy;


@Retention(RetentionPolicy.RUNTIME)
@ParametersSuppliedBy(SpringQueryExamplesSupplier.class)
public @interface SpringQueryExamples {
	String contextLocation();
	String exampleList();
}