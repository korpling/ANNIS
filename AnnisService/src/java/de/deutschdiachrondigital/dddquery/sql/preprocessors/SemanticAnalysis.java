package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

// TODO: kann das gelöscht werden?
// - zwischen äußeren und inneren Expressions (in einem Prädikat) unterscheiden
// - außen: Pfade, boolsche Kombinationen von diesen, Gruppierungen
public class SemanticAnalysis extends DepthFirstAdapter {
	
	APathExpr topLevelPath = null;
	
	// FIXME: check auslagern
	private void checkExpr(PExpr expr) {
		if ( ! (expr instanceof APathExpr || expr instanceof AAndExpr || expr instanceof AOrExpr) )
			throw new RuntimeException("top level expressions can only be a path or a logical relation");
	}
	
	@Override
	public void outStart(Start node) {
		checkExpr(node.getPExpr());
	}

	@Override
	public void inAPathExpr(APathExpr node) {
		if (topLevelPath == null)
			topLevelPath = node;
	}
	
	@Override
	public void outAPathExpr(APathExpr node) {
		if (topLevelPath == node)
			topLevelPath = null;
	}
	
	@Override
	public void inAAndExpr(AAndExpr node) {
		if (topLevelPath == null)
			for (PExpr expr : node.getExpr())
				checkExpr(expr);
	}
	
	@Override
	public void inAOrExpr(AOrExpr node) {
		if (topLevelPath == null)
			for (PExpr expr : node.getExpr())
				checkExpr(expr);
	}
}
