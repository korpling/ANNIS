package ex.annis.ql.parser;

import ex.annis.ql.analysis.DepthFirstAdapter;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.AAnyNodeSearchExpr;


public class NodeSearchNormalizer extends DepthFirstAdapter {

	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		String type = node.getAnnoType().getText();
		
		if (type.equals("node")) {
			AAnyNodeSearchExpr anyNodeSearchExpr = new AAnyNodeSearchExpr();
			node.replaceBy(anyNodeSearchExpr);
		}
	}
	
}
