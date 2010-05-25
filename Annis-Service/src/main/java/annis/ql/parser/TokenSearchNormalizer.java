package annis.ql.parser;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.ATextSearchNotEqualExpr;
import annis.ql.node.AUnequalAnnoValue;
import annis.ql.node.PAnnoValue;


public class TokenSearchNormalizer extends DepthFirstAdapter {

	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		String type = node.getAnnoType().getText();
		
		if (type.equals("tok"))
    {
			PAnnoValue annoValue = node.getAnnoValue();

      if(annoValue != null && annoValue instanceof AEqualAnnoValue)
      {
        ATextSearchExpr textSearchExpr = new ATextSearchExpr();
        textSearchExpr.setTextSpec(((AEqualAnnoValue) annoValue).getTextSpec());
        node.replaceBy(textSearchExpr);
      }
      else if(annoValue != null && annoValue instanceof AUnequalAnnoValue)
      {
        ATextSearchNotEqualExpr textSearchExpr = new ATextSearchNotEqualExpr();
        textSearchExpr.setTextSpec(((AUnequalAnnoValue) annoValue).getTextSpec());
        node.replaceBy(textSearchExpr);
      }
      else
      {
        ATextSearchExpr textSearchExpr = new ATextSearchExpr();
        textSearchExpr.setTextSpec(null);
        node.replaceBy(textSearchExpr);
      }
		}
	}
	
}
