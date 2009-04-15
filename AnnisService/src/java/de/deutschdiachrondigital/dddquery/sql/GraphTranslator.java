package de.deutschdiachrondigital.dddquery.sql;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;

public class GraphTranslator extends DepthFirstAdapter {
	
	AliasSetProvider aliasSetProvider;
	AbstractPathTranslator pathTranslator;
	Graph graph;
	
	@Override
	public void caseAPathExpr(APathExpr node) {
		aliasSetProvider.newAlternative(); // FIXME Test
		pathTranslator.setContext(null); // FIXME Test
		Path path = pathTranslator.translate(node);
		graph.addAlternative(path);
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		aliasSetProvider.newAlternative(); // FIXME Test
		Path path = new Path();
		for (PExpr expr : node.getExpr()) {
			if ( ! (expr instanceof APathExpr) )
				throw new RuntimeException("top-level expression must be a path or boolean combination");
			pathTranslator.setContext(null);  // FIXME Test
			path.merge(pathTranslator.translate(expr));
		}
		graph.addAlternative(path);
//		query.setInput(new Ast2StringShort().toString(node));
	}
	
	@Override
	public void inAOrExpr(AOrExpr node) {
		aliasSetProvider.startUnion();
	}
	
	@Override
	public void outAOrExpr(AOrExpr node) {
		aliasSetProvider.endUnion();
	}
	
	// FIXME test
	public Graph translate(Start node) {
		graph = new Graph();
		aliasSetProvider = pathTranslator.getAliasSetProvider();
		aliasSetProvider.reset();	// FIXME test
		node.apply(this);
		return graph;
	}
	
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph query) {
		this.graph = query;
	}
	
	public AbstractPathTranslator getPathTranslator() {
		return pathTranslator;
	}
	
	public void setPathTranslator(AbstractPathTranslator pathTranslator) {
		this.pathTranslator = pathTranslator;
	}


	public AliasSetProvider getAliasSetProvider() {
		return aliasSetProvider;
	}
	
	public void setAliasSetProvider(AliasSetProvider aliasSetProvider) {
		this.aliasSetProvider = aliasSetProvider;
	}

}
