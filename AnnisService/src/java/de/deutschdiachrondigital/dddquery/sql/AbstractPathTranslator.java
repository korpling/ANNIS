package de.deutschdiachrondigital.dddquery.sql;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.Ast2String;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Path;

public class AbstractPathTranslator extends DepthFirstAdapter {

	protected Path path;
	protected AliasSetProvider aliasSetProvider;
	protected AliasSet context;
	protected AliasSet target;

	public AbstractPathTranslator() {
		super();
	}

	public Path translate(Node node) {
		path = new Path();
		path.setInput(new Ast2String().toString(node));
		node.apply(this);
		return path;
	}

	public AliasSetProvider getAliasSetProvider() {
		return aliasSetProvider;
	}

	public void setAliasSetProvider(AliasSetProvider aliasSetProvider) {
		this.aliasSetProvider = aliasSetProvider;
	}

	public AliasSet getContext() {
		return context;
	}

	public void setContext(AliasSet context) {
		this.context = context;
	}

	public AliasSet getTarget() {
		return target;
	}


	public void setTarget(AliasSet target) {
		this.target = target;
	}

}