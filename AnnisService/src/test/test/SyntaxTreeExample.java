/**
 * 
 */
package test;

import org.junit.experimental.theories.PotentialParameterValue;

public class SyntaxTreeExample extends PotentialParameterValue {
	private String query;
	private String syntaxTree;

	@Override
	public Object getValue() throws CouldNotGenerateValueException {
		return this;
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getSyntaxTree() {
		return syntaxTree;
	}
	public void setSyntaxTree(String syntaxTree) {
		this.syntaxTree = syntaxTree;
	}
}