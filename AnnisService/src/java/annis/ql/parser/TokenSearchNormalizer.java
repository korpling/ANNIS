package annis.ql.parser;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.ATextSearchExpr;


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
