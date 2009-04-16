/**
 * 
 */
package test;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialParameterValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringQueryExamplesSupplier extends ParameterSupplier {

	@SuppressWarnings("unchecked")
	@Override
	public List getValueSources(Object test, ParameterSignature signature) {
		SpringQueryExamples annotation = (SpringQueryExamples) signature.getSupplierAnnotation();
		ApplicationContext ctx = new ClassPathXmlApplicationContext(annotation.contextLocation());
		List<String> exampleList = (List<String>) ctx.getBean(annotation.exampleList());
		List<PotentialParameterValue> examples = new ArrayList<PotentialParameterValue>();
		for (final String example : exampleList)
			examples.add(new PotentialParameterValue() {

				@Override
				public Object getValue()
						throws CouldNotGenerateValueException {
					return example;
				}
				
			});
		return examples;
	}
}