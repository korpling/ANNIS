package annis.ql.parser;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;


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
