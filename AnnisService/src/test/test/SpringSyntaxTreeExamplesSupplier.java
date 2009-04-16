/**
 * 
 */
package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringSyntaxTreeExamplesSupplier extends ParameterSupplier {

	@SuppressWarnings("unchecked")
	@Override
	public List getValueSources(Object test, ParameterSignature signature) {
		SpringSyntaxTreeExamples annotation = (SpringSyntaxTreeExamples) signature.getSupplierAnnotation();
		ApplicationContext ctx = new ClassPathXmlApplicationContext(annotation.contextLocation());
		Map<String, String> exampleMap = (Map<String, String>) ctx.getBean(annotation.exampleMap());
		List<SyntaxTreeExample> examples = new ArrayList<SyntaxTreeExample>();
		for (Entry<String, String> exampleEntry : exampleMap.entrySet()) {
			SyntaxTreeExample example = new SyntaxTreeExample();
			example.setQuery(exampleEntry.getKey());
			example.setSyntaxTree(exampleEntry.getValue().trim());
			examples.add(example);
		}
		return examples;
	}
	
}