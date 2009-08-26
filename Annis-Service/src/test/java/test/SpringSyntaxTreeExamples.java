/**
 * 
 */
package test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.experimental.theories.ParametersSuppliedBy;

@Retention(RetentionPolicy.RUNTIME)
@ParametersSuppliedBy(SpringSyntaxTreeExamplesSupplier.class)
public @interface SpringSyntaxTreeExamples {
	String contextLocation();
	String exampleMap();
}