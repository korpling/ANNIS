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