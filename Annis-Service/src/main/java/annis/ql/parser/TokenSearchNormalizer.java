package annis.ql.parser;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.AUnequalAnnoValue;
import annis.ql.node.PAnnoValue;


public class TokenSearchNormalizer extends DepthFirstAdapter {

	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		String type = node.getAnnoType().getText();
		
		if (type.equals("tok"))
    {
			ATextSearchExpr textSearchExpr = new ATextSearchExpr();
      PAnnoValue annoValue = node.getAnnoValue();
      if(annoValue != null && annoValue instanceof AEqualAnnoValue)
      {
        textSearchExpr.setTextSpec(((AEqualAnnoValue) annoValue).getTextSpec());
      }
      else if(annoValue != null && annoValue instanceof AUnequalAnnoValue)
      {
        textSearchExpr.setTextSpec(((AUnequalAnnoValue) annoValue).getTextSpec());
      }
      else
      {
        textSearchExpr.setTextSpec(null);
      }
			node.replaceBy(textSearchExpr);
		}
	}
	
}
