package ex.annis.ql.parser;

import ex.annis.ql.analysis.DepthFirstAdapter;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.ATextSearchExpr;


public class TokenSearchNormalizer extends DepthFirstAdapter {

	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		String type = node.getAnnoType().getText();
		
		if (type.equals("tok")) {
			ATextSearchExpr textSearchExpr = new ATextSearchExpr();
			textSearchExpr.setTextSpec(node.getAnnoValue());
			node.replaceBy(textSearchExpr);
		}
	}
	
}
